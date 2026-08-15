package qizhang.playerleash;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

final class PlayerLeashManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int POLICY_CHECK_INTERVAL_TICKS = 20;
    private static final int EFFECT_REFRESH_INTERVAL_TICKS = 20;
    private static final int HEART_PARTICLE_INTERVAL_TICKS = 10;
    private static final double ELASTIC_RANGE = 6.0D;
    private static final double PULL_PER_EXCESS_BLOCK = 0.1D;
    private static final double MAX_PULL_PER_TICK = 0.4D;

    private final Map<UUID, Boolean> consumedLeadByTarget = new HashMap<>();
    private final Map<UUID, TamingProgress> tamingProgressByTarget = new HashMap<>();
    private MinecraftServer server;
    private LeashRuleStore rules;
    private int tickCounter;

    synchronized LeashRuleStore rules(MinecraftServer currentServer) {
        ensureServer(currentServer);
        return rules;
    }

    synchronized boolean tryAttach(ServerPlayer holder, ServerPlayer target, ItemStack lead) {
        ensureServer(holder.getServer());
        if (!holder.isAlive() || !target.isAlive() || holder.isSpectator() || target.isSpectator()) {
            holder.sendSystemMessage(error("旁观者或已死亡玩家不能使用玩家拴绳。"));
            return false;
        }
        if (holder.level() != target.level()) {
            holder.sendSystemMessage(error("只能拴住同一维度内的玩家。"));
            return false;
        }
        if (!rules.isAllowed(holder.getGameProfile().getName(), target.getGameProfile().getName())) {
            holder.sendSystemMessage(error("后台规则禁止你拴住该玩家。"));
            return false;
        }

        PlayerTetherAccess targetTether = tether(target);
        if (targetTether.qizhang$getLeashHolderId() != 0) {
            holder.sendSystemMessage(error("该玩家已经被拴住了。"));
            return false;
        }
        if (wouldCreateCycle(holder, target)) {
            holder.sendSystemMessage(error("不能形成循环拴绳。"));
            return false;
        }

        boolean consumed = !holder.getAbilities().instabuild;
        targetTether.qizhang$setLeashHolder(holder);
        if (targetTether.qizhang$getLeashHolderId() != holder.getId()) {
            holder.sendSystemMessage(error("拴绳建立失败，物品未消耗。"));
            return false;
        }
        if (consumed) {
            lead.shrink(1);
        }
        consumedLeadByTarget.put(target.getUUID(), consumed);
        tamingProgressByTarget.put(target.getUUID(), TamingProgress.from(target));
        holder.sendSystemMessage(success("你已用拴绳拴住 " + target.getGameProfile().getName() + "。"));
        target.sendSystemMessage(Component.literal(
                holder.getGameProfile().getName() + " 用拴绳拴住了你。")
                .withStyle(ChatFormatting.GOLD));
        LOGGER.info("[七章玩家拴绳] {} -> {} attached (leadConsumed={}, distance={})",
                holder.getGameProfile().getName(), target.getGameProfile().getName(), consumed,
                String.format(java.util.Locale.ROOT, "%.2f", holder.distanceTo(target)));
        return true;
    }

    synchronized boolean release(
            ServerPlayer target,
            boolean broadcast,
            boolean requestLeadDrop,
            String message) {
        PlayerTetherAccess targetTether = tether(target);
        int holderId = targetTether.qizhang$getLeashHolderId();
        Entity holder = targetTether.qizhang$getLeashHolder();
        if (holderId == 0) {
            consumedLeadByTarget.remove(target.getUUID());
            tamingProgressByTarget.remove(target.getUUID());
            return false;
        }

        String holderName = holder == null ? "entity#" + holderId : holder.getName().getString();
        // SynchedEntityData broadcasts this change through vanilla tracking. The argument is
        // retained so all platform entrypoints can share the same manager contract.
        targetTether.qizhang$setLeashHolder(null);
        boolean actualDrop = QizhangPlayerLeash.shouldDropConsumedLead(target, requestLeadDrop);
        if (actualDrop && !target.level().isClientSide) {
            target.spawnAtLocation(Items.LEAD);
        }
        consumedLeadByTarget.remove(target.getUUID());
        tamingProgressByTarget.remove(target.getUUID());
        if (message != null && !message.isBlank()) {
            target.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.YELLOW));
            if (holder instanceof ServerPlayer holderPlayer && holderPlayer != target) {
                holderPlayer.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.YELLOW));
            }
        }
        LOGGER.info("[七章玩家拴绳] {} -> {} released (broadcast={})",
                holderName, target.getGameProfile().getName(), broadcast);
        return true;
    }

    synchronized int releaseInvolving(ServerPlayer player, boolean dropLead, String message) {
        MinecraftServer currentServer = player.getServer();
        if (currentServer == null) {
            return 0;
        }
        ensureServer(currentServer);
        int released = 0;
        for (ServerPlayer candidate : ListSnapshot.players(currentServer)) {
            PlayerTetherAccess candidateTether = tether(candidate);
            if (candidate == player || candidateTether.qizhang$getLeashHolderId() == player.getId()) {
                if (release(candidate, true, dropLead, message)) {
                    released++;
                }
            }
        }
        return released;
    }

    synchronized int releaseAll(MinecraftServer currentServer, boolean dropLead, String message) {
        ensureServer(currentServer);
        int released = 0;
        for (ServerPlayer candidate : ListSnapshot.players(currentServer)) {
            if (release(candidate, true, dropLead, message)) {
                released++;
            }
        }
        return released;
    }

    synchronized int releaseNamed(MinecraftServer currentServer, String targetName, String message) {
        ensureServer(currentServer);
        for (ServerPlayer player : ListSnapshot.players(currentServer)) {
            if (player.getGameProfile().getName().equalsIgnoreCase(targetName)) {
                return release(player, true, true, message) ? 1 : 0;
            }
        }
        return 0;
    }

    synchronized int releaseDisallowed(MinecraftServer currentServer) {
        ensureServer(currentServer);
        int released = 0;
        for (ServerPlayer target : ListSnapshot.players(currentServer)) {
            Entity holderEntity = tether(target).qizhang$getLeashHolder();
            if (!(holderEntity instanceof ServerPlayer holder)) {
                continue;
            }
            if (!rules.isAllowed(holder.getGameProfile().getName(), target.getGameProfile().getName())) {
                if (release(target, true, true, "后台规则已变更，这条拴绳被解除。")) {
                    released++;
                }
            }
        }
        return released;
    }

    synchronized void tick(MinecraftServer currentServer) {
        ensureServer(currentServer);
        tickLeashes(currentServer);
        tickTaming(currentServer);
        tickCounter++;
        if (tickCounter < POLICY_CHECK_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;

        for (ServerPlayer target : ListSnapshot.players(currentServer)) {
            MobEffectInstance displayedEffect = target.getEffect(QizhangPlayerLeash.tamedEffect());
            if (displayedEffect != null
                    && displayedEffect.getAmplifier() + 1 >= TamingSchedule.MAX_LAYERS
                    && target.tickCount % HEART_PARTICLE_INTERVAL_TICKS == 0
                    && target.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.HEART,
                        target.getX(),
                        target.getY() + 1.15D,
                        target.getZ(),
                        2,
                        0.35D,
                        0.35D,
                        0.35D,
                        0.01D);
            }

            Entity holderEntity = tether(target).qizhang$getLeashHolder();
            if (!(holderEntity instanceof ServerPlayer holder)) {
                continue;
            }
            if (!rules.isAllowed(holder.getGameProfile().getName(), target.getGameProfile().getName())) {
                release(target, true, true, "后台规则禁止这条拴绳，已自动解除。");
            }
        }
    }

    synchronized boolean consumeDropDecision(ServerPlayer target, boolean requestedDrop) {
        Boolean consumed = consumedLeadByTarget.remove(target.getUUID());
        tamingProgressByTarget.remove(target.getUUID());
        return requestedDrop && (consumed == null || consumed);
    }

    synchronized void stop(MinecraftServer currentServer) {
        if (server == currentServer) {
            releaseAll(currentServer, true, "服务器关闭，拴绳已解除。");
            consumedLeadByTarget.clear();
            tamingProgressByTarget.clear();
            server = null;
            rules = null;
            tickCounter = 0;
        }
    }

    private void tickLeashes(MinecraftServer currentServer) {
        Set<UUID> activeTargets = new HashSet<>();
        for (ServerPlayer target : ListSnapshot.players(currentServer)) {
            PlayerTetherAccess targetTether = tether(target);
            if (targetTether.qizhang$getLeashHolderId() == 0) {
                continue;
            }

            Entity holderEntity = targetTether.qizhang$getLeashHolder();
            if (!(holderEntity instanceof ServerPlayer holder)
                    || holder.getServer() != currentServer
                    || !holder.isAlive()
                    || !target.isAlive()
                    || holder.level() != target.level()) {
                release(target, true, true, "拴绳连接失效，已自动解除。");
                continue;
            }

            double distanceSquared = target.distanceToSqr(holder);
            activeTargets.add(target.getUUID());
            if (distanceSquared > ELASTIC_RANGE * ELASTIC_RANGE) {
                applyElasticPull(target, holder, Math.sqrt(distanceSquared));
            }
        }
        consumedLeadByTarget.keySet().removeIf(uuid -> !activeTargets.contains(uuid));
    }

    private static void applyElasticPull(ServerPlayer target, ServerPlayer holder, double distance) {
        Vec3 towardHolder = holder.position().subtract(target.position());
        double acceleration = Math.min(
                MAX_PULL_PER_TICK,
                (distance - ELASTIC_RANGE) * PULL_PER_EXCESS_BLOCK);
        if (distance > 1.0E-5D && acceleration > 0.0D) {
            target.setDeltaMovement(target.getDeltaMovement().add(towardHolder.scale(acceleration / distance)));
            target.hurtMarked = true;
            target.hasImpulse = true;
            target.resetFallDistance();
        }
    }

    private void ensureServer(MinecraftServer currentServer) {
        if (currentServer == null) {
            throw new IllegalStateException("服务器尚未就绪");
        }
        if (server == currentServer && rules != null) {
            return;
        }
        server = currentServer;
        tickCounter = 0;
        consumedLeadByTarget.clear();
        tamingProgressByTarget.clear();
        Path config = currentServer.getWorldPath(LevelResource.ROOT)
                .resolve("serverconfig")
                .resolve("qizhang-player-leash.properties");
        rules = new LeashRuleStore(config);
        try {
            rules.load();
            LOGGER.info("[七章玩家拴绳] 已加载规则：default={}, rules={}, file={}",
                    rules.defaultAllowed() ? "allow" : "deny", rules.ruleCount(), config);
        } catch (IOException exception) {
            LOGGER.error("[七章玩家拴绳] 无法加载规则文件，当前会话使用默认全允许且不会覆盖损坏文件：{}",
                    config, exception);
        }
    }

    private static boolean wouldCreateCycle(ServerPlayer holder, ServerPlayer target) {
        Entity cursor = holder;
        Set<UUID> visited = new HashSet<>();
        for (int depth = 0; depth < 64; depth++) {
            if (cursor == target) {
                return true;
            }
            if (!visited.add(cursor.getUUID()) || !(cursor instanceof PlayerTetherAccess tetherAccess)) {
                return false;
            }
            cursor = tetherAccess.qizhang$getLeashHolder();
            if (cursor == null) {
                return false;
            }
        }
        return true;
    }

    private void tickTaming(MinecraftServer currentServer) {
        Set<UUID> activeTargets = new HashSet<>();
        for (ServerPlayer target : ListSnapshot.players(currentServer)) {
            Entity holderEntity = tether(target).qizhang$getLeashHolder();
            if (!(holderEntity instanceof ServerPlayer holder)
                    || !target.isAlive()
                    || !holder.isAlive()
                    || target.level() != holder.level()) {
                continue;
            }
            activeTargets.add(target.getUUID());
            TamingProgress progress = tamingProgressByTarget.computeIfAbsent(
                    target.getUUID(), ignored -> TamingProgress.from(target));
            MobEffectInstance existing = target.getEffect(QizhangPlayerLeash.tamedEffect());
            if (existing != null) {
                progress.layers = Math.max(progress.layers, Math.min(
                        TamingSchedule.MAX_LAYERS, existing.getAmplifier() + 1));
            }

            progress.continuousTicks++;
            boolean layerAdded = false;
            if (progress.layers < TamingSchedule.MAX_LAYERS
                    && progress.continuousTicks
                            >= TamingSchedule.secondsForNextLayer(progress.layers) * 20) {
                progress.continuousTicks = 0;
                progress.layers++;
                layerAdded = true;
            }

            if (progress.layers > 0
                    && (layerAdded || progress.continuousTicks % EFFECT_REFRESH_INTERVAL_TICKS == 0)) {
                int duration = TamingSchedule.effectDurationSeconds(progress.layers) * 20
                        + EFFECT_REFRESH_INTERVAL_TICKS;
                target.addEffect(new MobEffectInstance(
                        QizhangPlayerLeash.tamedEffect(),
                        duration,
                        progress.layers - 1,
                        false,
                        false,
                        true));
            }

            if (layerAdded) {
                target.sendSystemMessage(Component.literal(
                        "[驯服彩蛋] 驯服效果提升到 " + progress.layers + "/"
                                + TamingSchedule.MAX_LAYERS
                                + " 层；松开后保留约 "
                                + TamingSchedule.effectDurationSeconds(progress.layers) + " 秒。")
                        .withStyle(progress.layers >= 3 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GOLD));
                holder.sendSystemMessage(Component.literal(
                        "[驯服彩蛋] " + target.getGameProfile().getName() + " 的驯服效果达到 "
                                + progress.layers + "/" + TamingSchedule.MAX_LAYERS + " 层。")
                        .withStyle(ChatFormatting.GOLD));
            }
        }
        tamingProgressByTarget.keySet().removeIf(uuid -> !activeTargets.contains(uuid));
    }

    private static PlayerTetherAccess tether(ServerPlayer player) {
        return (PlayerTetherAccess) player;
    }

    private static Component error(String text) {
        return Component.literal("[玩家拴绳] " + text).withStyle(ChatFormatting.RED);
    }

    private static Component success(String text) {
        return Component.literal("[玩家拴绳] " + text).withStyle(ChatFormatting.GREEN);
    }

    private static final class ListSnapshot {
        private ListSnapshot() {
        }

        private static java.util.List<ServerPlayer> players(MinecraftServer server) {
            return java.util.List.copyOf(server.getPlayerList().getPlayers());
        }
    }

    private static final class TamingProgress {
        private int continuousTicks;
        private int layers;

        private static TamingProgress from(ServerPlayer player) {
            TamingProgress progress = new TamingProgress();
            MobEffectInstance existing = player.getEffect(QizhangPlayerLeash.tamedEffect());
            if (existing != null) {
                progress.layers = Math.min(TamingSchedule.MAX_LAYERS, existing.getAmplifier() + 1);
            }
            return progress;
        }
    }
}
