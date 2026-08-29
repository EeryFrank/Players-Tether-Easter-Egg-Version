// SPDX-License-Identifier: GPL-3.0-only

package qizhang.playerleash.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qizhang.playerleash.PlayerTetherAccess;

@Mixin(Player.class)
public abstract class PlayerLeashMixin implements PlayerTetherAccess {
    @Unique
    private static final EntityDataAccessor<Integer> QIZHANG$LEASH_HOLDER_ID =
            SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void qizhang$definePlayerLeashData(CallbackInfo callbackInfo) {
        qizhang$self().getEntityData().define(QIZHANG$LEASH_HOLDER_ID, 0);
    }

    @Override
    public Entity qizhang$getLeashHolder() {
        int holderId = qizhang$getLeashHolderId();
        return holderId == 0 ? null : qizhang$self().level().getEntity(holderId);
    }

    @Override
    public int qizhang$getLeashHolderId() {
        return qizhang$self().getEntityData().get(QIZHANG$LEASH_HOLDER_ID);
    }

    @Override
    public void qizhang$setLeashHolder(Entity holder) {
        qizhang$self().getEntityData().set(QIZHANG$LEASH_HOLDER_ID, holder == null ? 0 : holder.getId());
    }

    @Unique
    private Entity qizhang$self() {
        return (Entity) (Object) this;
    }
}
