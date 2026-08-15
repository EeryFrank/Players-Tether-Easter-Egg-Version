package qizhang.playerleash.mixin;

import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
    public void leashTooFarBehaviour() {
        Entity self = (Entity) (Object) this;
        Entity holder = getLeashHolder();
        if (holder == null || !self.isAlive() || !holder.isAlive()) {
            dropLeash(true, true);
            return;
        }

        // Vanilla drops a leash immediately beyond ten blocks. Player tethers
        // remain elastic instead; explicit lifecycle and policy checks still
        // release invalid links through PlayerLeashManager.
        qizhang$keepElasticBeyondVanillaRange(holder, self);
    }

    @Unique
    private void qizhang$keepElasticBeyondVanillaRange(Entity holder, Entity self) {
        elasticRangeLeashBehaviour(holder, self.distanceTo(holder));
    }

    @Override
    public void setLeashedTo(Entity holder, boolean broadcastPacket) {
        Leashable.super.setLeashedTo(holder, broadcastPacket);
        Object self = this;
        if (broadcastPacket && self instanceof ServerPlayer serverPlayer) {
            // Vanilla's tracking broadcast excludes the tracked player itself. Send the
            // link explicitly so that the tethered player's third-person client also sees it.
            serverPlayer.connection.send(new ClientboundSetEntityLinkPacket(serverPlayer, holder));
        }
    }

    @Override
    public void dropLeash(boolean broadcastPacket, boolean dropLead) {
        Object self = this;
        boolean actualDrop = self instanceof ServerPlayer serverPlayer
                && QizhangPlayerLeash.shouldDropConsumedLead(serverPlayer, dropLead);
        Leashable.super.dropLeash(broadcastPacket, actualDrop);
        if (broadcastPacket && self instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetEntityLinkPacket(serverPlayer, null));
        }
    }
}
