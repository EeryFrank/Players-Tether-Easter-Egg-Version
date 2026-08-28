// SPDX-License-Identifier: LGPL-3.0-or-later

package qizhang.playerleash;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod(QizhangPlayerLeash.MOD_ID)
public final class QizhangPlayerLeash {
    public static final String MOD_ID = "qizhang_player_leash";
    public static final String VERSION = "1.1.6";

    private static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, MOD_ID);
    private static final RegistryObject<TamedMobEffect> TAMED_EFFECT =
            EFFECTS.register("tamed", TamedMobEffect::new);
    private static final PlayerLeashManager MANAGER = new PlayerLeashManager();

    public QizhangPlayerLeash() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        EFFECTS.register(modBus);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onEntityInteract);
        MinecraftForge.EVENT_BUS.addListener(this::onLivingDeath);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerChangedDimension);
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);
    }

    static PlayerLeashManager manager() {
        return MANAGER;
    }

    public static boolean shouldDropConsumedLead(ServerPlayer target, boolean requestedDrop) {
        return MANAGER.consumeDropDecision(target, requestedDrop);
    }

    public static MobEffect tamedEffect() {
        return TAMED_EFFECT.get();
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

        Entity currentHolder = ((PlayerTetherAccess) (Object) target).qizhang$getLeashHolder();
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

    private void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MANAGER.releaseInvolving(player, true, "玩家死亡，拴绳已解除。");
        }
    }

    private void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MANAGER.releaseInvolving(player, true, "玩家切换维度，拴绳已解除。");
        }
    }

    private void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            MANAGER.tick(event.getServer());
        }
    }

    private void onServerStopping(ServerStoppingEvent event) {
        MANAGER.stop(event.getServer());
    }
}
