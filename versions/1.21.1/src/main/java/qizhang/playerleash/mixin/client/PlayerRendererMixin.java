// SPDX-License-Identifier: LGPL-3.0-or-later

package qizhang.playerleash.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qizhang.playerleash.client.TamedPlayerRenderer;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    @Inject(
            method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true)
    private void qizhang$renderTamedPlayerAsWolf(
            AbstractClientPlayer player,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            CallbackInfo callbackInfo) {
        if (TamedPlayerRenderer.renderIfTamed(
                player, entityYaw, partialTicks, poseStack, buffers, packedLight)) {
            callbackInfo.cancel();
        }
    }
}
