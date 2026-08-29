// SPDX-License-Identifier: GPL-3.0-only

package qizhang.playerleash.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import qizhang.playerleash.QizhangPlayerLeash;

@EventBusSubscriber(modid = QizhangPlayerLeash.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeClientRenderEvents {
    private NeoForgeClientRenderEvents() {
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
                event.getPartialTick().getGameTimeDeltaPartialTick(false),
                buffers)) {
            buffers.endBatch(RenderType.leash());
        }
    }
}
