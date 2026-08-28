// SPDX-License-Identifier: LGPL-3.0-or-later

package qizhang.playerleash.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import qizhang.playerleash.QizhangPlayerLeash;

@Mod.EventBusSubscriber(
        modid = QizhangPlayerLeash.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class ForgeLikeClientRenderEvents {
    private ForgeLikeClientRenderEvents() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        if (FirstPersonLeashRenderer.render(
                event.getPoseStack(),
                event.getCamera(),
                event.getPartialTick(),
                buffers)) {
            buffers.endBatch(RenderType.leash());
        }
    }
}
