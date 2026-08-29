// SPDX-License-Identifier: GPL-3.0-only

package qizhang.playerleash.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import qizhang.playerleash.mixin.client.EntityRendererInvoker;

/** Adds the local player's otherwise-hidden tether to first-person world rendering. */
public final class FirstPersonLeashRenderer {
    private FirstPersonLeashRenderer() {
    }

    public static boolean render(
            PoseStack poseStack,
            Camera camera,
            float partialTick,
            MultiBufferSource buffers) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (!shouldRenderLocalFirstPersonLeash(minecraft, player)) {
            return false;
        }

        Leashable playerLeash = (Leashable) (Object) player;
        Entity holder = playerLeash.getLeashHolder();
        if (holder == null || !holder.isAlive() || holder.level() != player.level()) {
            return false;
        }

        double x = Mth.lerp((double) partialTick, player.xo, player.getX())
                - camera.getPosition().x;
        double y = Mth.lerp((double) partialTick, player.yo, player.getY())
                - camera.getPosition().y;
        double z = Mth.lerp((double) partialTick, player.zo, player.getZ())
                - camera.getPosition().z;

        poseStack.pushPose();
        try {
            poseStack.translate(x, y, z);
            EntityRendererInvoker renderer = (EntityRendererInvoker) minecraft
                    .getEntityRenderDispatcher()
                    .getRenderer(player);
            renderer.qizhang$renderLeash(player, partialTick, poseStack, buffers, holder);
        } finally {
            poseStack.popPose();
        }
        return true;
    }

    private static boolean shouldRenderLocalFirstPersonLeash(
            Minecraft minecraft,
            LocalPlayer player) {
        return player != null
                && minecraft.getCameraEntity() == player
                && minecraft.options.getCameraType().isFirstPerson()
                && player.isAlive();
    }
}
