// SPDX-License-Identifier: LGPL-3.0-or-later

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
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

final class PlayerLeashManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int POLICY_CHECK_INTERVAL_TICKS = 20;
    private static final int EFFECT_REFRESH_INTERVAL_TICKS = 20;
    private static final int HEART_PARTICLE_INTERVAL_TICKS = 10;

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
        if (!rules.isAllowed(holder.getGameProfile().getName(), target.getGameProfile().getName())) {
            holder.sendSystemMessage(error("后台规则禁止你拴住该玩家。"));
            return false;
        }

        Leashable targetLeash = (Leashable) (Object) target;
        Entity currentHolder = targetLeash.getLeashHolder();
        if (currentHolder != null) {
            holder.sendSystemMessage(error("该玩家已经被拴住了。"));
            return false;
        }
        if (wouldCreateCycle(holder, target)) {
            holder.sendSystemMessage(error("不能形成循环拴绳。"));
            return false;
        }

        boolean consumed = !holder.getAbilities().instabuild;
        targetLeash.setLeashedTo(holder, true);
        if (targetLeash.getLeashHolder() != holder) {
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
        Leashable leashable = (Leashable) (Object) target;
        Entity holder = leashable.getLeashHolder();
        if (holder == null) {
            return settleVanillaClearedLeash(target, requestLeadDrop, message);
        }
        String holderName = holder.getName().getString();
        leashable.dropLeash(broadcast, requestLeadDrop);
        consumedLeadByTarget.remove(target.getUUID());
        tamingProgressByTarget.remove(target.getUUID());
        if (message != null && !message.isBlank()) {
            target.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.YELLOW));
            if (holder instanceof ServerPlayer holderPlayer && holderPlayer != target) {
                holderPlayer.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.YELLOW));
            }
        }
        LOGGER.info("[七章玩家拴绳] {} -> {} released",
                holderName, target.getGameProfile().getName());
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
            Leashable leashable = (Leashable) (Object) candidate;
            Entity holder = leashable.getLeashHolder();
            if (candidate == player || holder == player) {
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
            Leashable leashable = (Leashable) (Object) target;
            Entity holderEntity = leashable.getLeashHolder();
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
        tickTaming(currentServer);
        tickCounter++;
        if (tickCounter < POLICY_CHECK_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;

        Set<UUID> activeTargets = new HashSet<>();
        for (ServerPlayer target : ListSnapshot.players(currentServer)) {
            Leashable leashable = (Leashable) (Object) target;
            Entity holderEntity = leashable.getLeashHolder();
            if (holderEntity == null) {
                continue;
            }
            activeTargets.add(target.getUUID());
            if (!(holderEntity instanceof ServerPlayer holder)
                    || holder.getServer() != currentServer
                    || !holder.isAlive()
                    || !target.isAlive()
                    || holder.level() != target.level()) {
                release(target, true, true, "拴绳连接失效，已自动解除。");
                continue;
            }
            if (!rules.isAllowed(holder.getGameProfile().getName(), target.getGameProfile().getName())) {
                release(target, true, true, "后台规则禁止这条拴绳，已自动解除。");
            }
        }
        consumedLeadByTarget.keySet().removeIf(uuid -> !activeTargets.contains(uuid));
    }

    synchronized boolean consumeDropDecision(ServerPlayer target, boolean requestedDrop) {
        if (!requestedDrop) {
            // Dimension transfer clears vanilla's LeashData before the platform's
            // after-change event. Preserve the consumption decision so that event
            // can still refund a survival lead without creating one for creative.
            tamingProgressByTarget.remove(target.getUUID());
            return false;
        }
        Boolean consumed = consumedLeadByTarget.remove(target.getUUID());
        tamingProgressByTarget.remove(target.getUUID());
        return consumed == null || consumed;
    }

    private boolean settleVanillaClearedLeash(
            ServerPlayer target,
            boolean requestLeadDrop,
            String message) {
        Boolean consumed = consumedLeadByTarget.remove(target.getUUID());
        tamingProgressByTarget.remove(target.getUUID());
        if (consumed == null) {
            return false;
        }

        boolean actualDrop = requestLeadDrop && consumed;
        if (actualDrop && !target.level().isClientSide()) {
            target.spawnAtLocation(Items.LEAD);
        }
        if (message != null && !message.isBlank()) {
            target.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.YELLOW));
        }
        LOGGER.info("[七章玩家拴绳] vanilla-cleared -> {} released (leadDropped={})",
                target.getGameProfile().getName(), actualDrop);
        return true;
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
            if (!visited.add(cursor.getUUID()) || !(cursor instanceof Leashable leashable)) {
                return false;
            }
            cursor = leashable.getLeashHolder();
            if (cursor == null) {
                return false;
            }
        }
        return true;
    }

    private void tickTaming(MinecraftServer currentServer) {
        Set<UUID> activeTargets = new HashSet<>();
        for (ServerPlayer target : ListSnapshot.players(currentServer)) {
            Leashable leashable = (Leashable) (Object) target;
            Entity holderEntity = leashable.getLeashHolder();
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
                MobEffectInstance refreshedEffect = new MobEffectInstance(
                        QizhangPlayerLeash.tamedEffect(),
                        duration,
                        progress.layers - 1,
                        false,
                        false,
                        true);
                target.addEffect(refreshedEffect);

                // ServerPlayer synchronizes its own effect to its own connection.
                // The wolf-model renderer also needs the same update on every
                // client tracking this player.
                MobEffectInstance appliedEffect = target.getEffect(QizhangPlayerLeash.tamedEffect());
                if (appliedEffect != null) {
                    broadcastTamingEffectToTrackingClients(target, appliedEffect);
                }
            }

            // This method runs every server tick. Keeping the level-six heart
            // cadence here prevents a 20-tick policy phase from permanently
            // missing the independent 10-tick particle phase.
            if (TamedVisualRules.showsHeartParticles(progress.layers - 1)
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

            if (layerAdded) {
                holder.sendSystemMessage(Component.literal(
                        "[驯服彩蛋] " + target.getGameProfile().getName() + " 的驯服效果达到 "
                                + progress.layers + "/" + TamingSchedule.MAX_LAYERS + " 层。")
                        .withStyle(ChatFormatting.GOLD));
            }

        }
        tamingProgressByTarget.keySet().removeIf(uuid -> !activeTargets.contains(uuid));
    }

    private static void broadcastTamingEffectToTrackingClients(
            ServerPlayer target,
            MobEffectInstance appliedEffect) {
        if (target.level() instanceof ServerLevel level) {
            level.getChunkSource().broadcastAndSend(
                    target,
                    new ClientboundUpdateMobEffectPacket(target.getId(), appliedEffect, false));
        }
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
