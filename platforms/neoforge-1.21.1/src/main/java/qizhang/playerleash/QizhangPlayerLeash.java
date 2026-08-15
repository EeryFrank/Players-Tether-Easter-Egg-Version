package qizhang.playerleash;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(QizhangPlayerLeash.MOD_ID)
public final class QizhangPlayerLeash {
    public static final String MOD_ID = "qizhang_player_leash";
    public static final String VERSION = "1.1.0";

    private static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, MOD_ID);
    private static final DeferredHolder<MobEffect, TamedMobEffect> TAMED_EFFECT =
            EFFECTS.register("tamed", TamedMobEffect::new);
    private static final PlayerLeashManager MANAGER = new PlayerLeashManager();

    public QizhangPlayerLeash(IEventBus modBus) {
        EFFECTS.register(modBus);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.EntityInteract.class, this::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedOutEvent.class, this::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerChangedDimensionEvent.class, this::onPlayerChangedDimension);
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, this::onServerTick);
        NeoForge.EVENT_BUS.addListener(ServerStoppingEvent.class, this::onServerStopping);
    }

    static PlayerLeashManager manager() {
        return MANAGER;
    }

    public static boolean shouldDropConsumedLead(ServerPlayer target, boolean requestedDrop) {
        return MANAGER.consumeDropDecision(target, requestedDrop);
    }

    public static Holder<MobEffect> tamedEffect() {
        return TAMED_EFFECT;
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        PlayerLeashCommands.register(event.getDispatcher());
    }

    private void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer holder)
                || !(event.getTarget() instanceof ServerPlayer target)
                || holder.level().isClientSide()
                || holder == target) {
            return;
        }

        Leashable targetLeash = (Leashable) (Object) target;
        Entity currentHolder = targetLeash.getLeashHolder();

        if (holder.isShiftKeyDown() && currentHolder == holder) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            MANAGER.release(target, true, true, "已解开拴绳。");
            return;
        }

        if (!event.getItemStack().is(Items.LEAD)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        MANAGER.tryAttach(holder, target, event.getItemStack());
    }

    private void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MANAGER.releaseInvolving(player, true, "玩家离线，拴绳已解除。");
        }
    }

    private void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MANAGER.releaseInvolving(player, true, "玩家切换维度，拴绳已解除。");
        }
    }

    private void onServerTick(ServerTickEvent.Post event) {
        MANAGER.tick(event.getServer());
    }

    private void onServerStopping(ServerStoppingEvent event) {
        MANAGER.stop(event.getServer());
    }
}
