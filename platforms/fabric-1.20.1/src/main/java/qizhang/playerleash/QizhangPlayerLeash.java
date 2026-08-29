// SPDX-License-Identifier: GPL-3.0-only

package qizhang.playerleash;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;

public final class QizhangPlayerLeash implements ModInitializer {
    public static final String MOD_ID = "qizhang_player_leash";
    public static final String VERSION = "1.1.6";

    private static final PlayerLeashManager MANAGER = new PlayerLeashManager();
    private static MobEffect tamedEffect;

    @Override
    public void onInitialize() {
        tamedEffect = Registry.register(
                BuiltInRegistries.MOB_EFFECT,
                new ResourceLocation(MOD_ID, "tamed"),
                new TamedMobEffect());

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> PlayerLeashCommands.register(dispatcher));
        UseEntityCallback.EVENT.register((player, level, hand, targetEntity, hitResult) -> {
            if (level.isClientSide()
                    || !(player instanceof ServerPlayer holder)
                    || !(targetEntity instanceof ServerPlayer target)
                    || holder == target) {
                return InteractionResult.PASS;
            }

            Entity currentHolder = ((PlayerTetherAccess) (Object) target).qizhang$getLeashHolder();
            if (holder.isShiftKeyDown() && currentHolder == holder) {
                MANAGER.release(target, true, true, "已解开拴绳。");
                return InteractionResult.SUCCESS;
            }
            if (!holder.getItemInHand(hand).is(Items.LEAD)) {
                return InteractionResult.PASS;
            }
            MANAGER.tryAttach(holder, target, holder.getItemInHand(hand));
            return InteractionResult.SUCCESS;
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                MANAGER.releaseInvolving(handler.getPlayer(), true, "玩家离线，拴绳已解除。"));
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player) {
                MANAGER.releaseInvolving(player, true, "玩家死亡，拴绳已解除。");
            }
        });
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) ->
                MANAGER.releaseInvolving(player, true, "玩家切换维度，拴绳已解除。"));
        ServerTickEvents.END_SERVER_TICK.register(MANAGER::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(MANAGER::stop);
    }

    static PlayerLeashManager manager() {
        return MANAGER;
    }

    public static boolean shouldDropConsumedLead(ServerPlayer target, boolean requestedDrop) {
        return MANAGER.consumeDropDecision(target, requestedDrop);
    }

    public static MobEffect tamedEffect() {
        if (tamedEffect == null) {
            throw new IllegalStateException("Tamed effect has not been registered yet");
        }
        return tamedEffect;
    }
}
