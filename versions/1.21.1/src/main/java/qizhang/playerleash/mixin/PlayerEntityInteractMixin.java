package qizhang.playerleash.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents vanilla's base Entity interaction from undoing a permission-checked
 * player tether during the same right click. The bypass is limited to a player
 * target already held by the interacting player, so unrelated player and mob
 * interactions keep their original behavior.
 */
@Mixin(Entity.class)
public abstract class PlayerEntityInteractMixin {
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void qizhang$skipVanillaLeashToggleForPlayerTarget(
            Player interactingPlayer,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> callbackInfo) {
        Object target = this;
        if (target instanceof Player
                && target instanceof Leashable leashable
                && leashable.getLeashHolder() == interactingPlayer) {
            callbackInfo.setReturnValue(InteractionResult.PASS);
        }
    }
}
