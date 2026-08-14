package qizhang.playerleash.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qizhang.playerleash.QizhangPlayerLeash;

@Mixin(Player.class)
public abstract class PlayerLeashMixin implements Leashable {
    @Unique
    private LeashData qizhang$playerLeashData;

    @Override
    public LeashData getLeashData() {
        return qizhang$playerLeashData;
    }

    @Override
    public void setLeashData(LeashData data) {
        qizhang$playerLeashData = data;
    }

    @Override
    public boolean canBeLeashed() {
        // Only the mod's permission-checked player interaction may establish a player leash.
        // This prevents vanilla fence-knot searches from bypassing pair rules.
        return false;
    }

    @Override
    public void elasticRangeLeashBehaviour(Entity holder, float distance) {
        Leashable.super.elasticRangeLeashBehaviour(holder, distance);
        Entity self = (Entity) (Object) this;
        self.hurtMarked = true;
        self.hasImpulse = true;
        self.resetFallDistance();
    }

    @Override
    public void dropLeash(boolean broadcastPacket, boolean dropLead) {
        Object self = this;
        boolean actualDrop = self instanceof ServerPlayer serverPlayer
                && QizhangPlayerLeash.shouldDropConsumedLead(serverPlayer, dropLead);
        Leashable.super.dropLeash(broadcastPacket, actualDrop);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void qizhang$tickPlayerLeash(CallbackInfo callbackInfo) {
        Leashable.tickLeash((Entity & Leashable) (Object) this);
    }
}
