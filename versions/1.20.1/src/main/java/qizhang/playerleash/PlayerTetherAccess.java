package qizhang.playerleash;

import net.minecraft.world.entity.Entity;

/**
 * Version bridge for the player leash state used on Minecraft 1.20.1.
 *
 * <p>The holder id is stored in {@code SynchedEntityData} by the player mixin,
 * so vanilla entity tracking also synchronizes it to late-joining clients.</p>
 */
public interface PlayerTetherAccess {
    Entity qizhang$getLeashHolder();

    int qizhang$getLeashHolderId();

    void qizhang$setLeashHolder(Entity holder);
}
