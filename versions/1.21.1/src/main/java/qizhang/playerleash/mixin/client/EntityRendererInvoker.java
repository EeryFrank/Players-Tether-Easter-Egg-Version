// SPDX-License-Identifier: LGPL-3.0-or-later

package qizhang.playerleash.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface EntityRendererInvoker {
    @Invoker("renderLeash")
    void qizhang$renderLeash(
            Entity entity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            Entity leashHolder);
}
