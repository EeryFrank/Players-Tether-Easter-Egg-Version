// SPDX-License-Identifier: LGPL-3.0-or-later

package qizhang.playerleash;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.renderer.MultiBufferSource;
import qizhang.playerleash.client.FirstPersonLeashRenderer;

public final class QizhangPlayerLeashClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            PoseStack poseStack = context.matrixStack();
            MultiBufferSource buffers = context.consumers();
            if (poseStack != null && buffers != null) {
                FirstPersonLeashRenderer.render(
                        poseStack,
                        context.camera(),
                        context.tickCounter().getGameTimeDeltaPartialTick(false),
                        buffers);
            }
        });
    }
}
