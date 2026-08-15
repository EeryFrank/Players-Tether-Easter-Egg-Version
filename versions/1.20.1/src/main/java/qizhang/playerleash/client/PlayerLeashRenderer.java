package qizhang.playerleash.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import qizhang.playerleash.PlayerTetherAccess;

/** Draws the 1.20.1 player tether using the same segmented curve as vanilla mob leashes. */
public final class PlayerLeashRenderer {
    private static final int PIECE_COUNT = 24;

    private PlayerLeashRenderer() {
    }

    public static void render(
            Player player,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers) {
        Entity holder = ((PlayerTetherAccess) player).qizhang$getLeashHolder();
        if (holder == null || holder.level() != player.level()) {
            return;
        }

        poseStack.pushPose();
        Vec3 holderPosition = holder.getRopeHoldPosition(partialTicks);
        double bodyAngle = Mth.lerp(partialTicks, player.yBodyRotO, player.yBodyRot)
                * (Math.PI / 180.0D) + (Math.PI / 2.0D);
        Vec3 offset = player.getLeashOffset(partialTicks);
        double offsetX = Math.cos(bodyAngle) * offset.z + Math.sin(bodyAngle) * offset.x;
        double offsetZ = Math.sin(bodyAngle) * offset.z - Math.cos(bodyAngle) * offset.x;
        double playerX = Mth.lerp((double) partialTicks, player.xo, player.getX()) + offsetX;
        double playerY = Mth.lerp((double) partialTicks, player.yo, player.getY()) + offset.y;
        double playerZ = Mth.lerp((double) partialTicks, player.zo, player.getZ()) + offsetZ;
        poseStack.translate(offsetX, offset.y, offsetZ);

        float deltaX = (float) (holderPosition.x - playerX);
        float deltaY = (float) (holderPosition.y - playerY);
        float deltaZ = (float) (holderPosition.z - playerZ);
        VertexConsumer consumer = buffers.getBuffer(RenderType.leash());
        Matrix4f pose = poseStack.last().pose();
        float horizontalLength = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float normalScale = horizontalLength < 1.0E-5F ? 0.0F : 0.0125F / horizontalLength;
        float normalX = deltaZ * normalScale;
        float normalZ = deltaX * normalScale;

        BlockPos playerLightPosition = BlockPos.containing(player.getEyePosition(partialTicks));
        BlockPos holderLightPosition = BlockPos.containing(holder.getEyePosition(partialTicks));
        int playerBlockLight = player.isOnFire()
                ? 15
                : player.level().getBrightness(LightLayer.BLOCK, playerLightPosition);
        int holderBlockLight = holder.isOnFire()
                ? 15
                : holder.level().getBrightness(LightLayer.BLOCK, holderLightPosition);
        int playerSkyLight = player.level().getBrightness(LightLayer.SKY, playerLightPosition);
        int holderSkyLight = holder.level().getBrightness(LightLayer.SKY, holderLightPosition);

        for (int piece = 0; piece <= PIECE_COUNT; piece++) {
            addVertexPair(
                    consumer,
                    pose,
                    deltaX,
                    deltaY,
                    deltaZ,
                    playerBlockLight,
                    holderBlockLight,
                    playerSkyLight,
                    holderSkyLight,
                    0.025F,
                    0.025F,
                    normalX,
                    normalZ,
                    piece,
                    false);
        }
        for (int piece = PIECE_COUNT; piece >= 0; piece--) {
            addVertexPair(
                    consumer,
                    pose,
                    deltaX,
                    deltaY,
                    deltaZ,
                    playerBlockLight,
                    holderBlockLight,
                    playerSkyLight,
                    holderSkyLight,
                    0.025F,
                    0.0F,
                    normalX,
                    normalZ,
                    piece,
                    true);
        }
        poseStack.popPose();
    }

    private static void addVertexPair(
            VertexConsumer consumer,
            Matrix4f pose,
            float deltaX,
            float deltaY,
            float deltaZ,
            int playerBlockLight,
            int holderBlockLight,
            int playerSkyLight,
            int holderSkyLight,
            float firstOffset,
            float secondOffset,
            float normalX,
            float normalZ,
            int piece,
            boolean reverseShade) {
        float progress = (float) piece / PIECE_COUNT;
        int blockLight = (int) Mth.lerp(progress, (float) playerBlockLight, (float) holderBlockLight);
        int skyLight = (int) Mth.lerp(progress, (float) playerSkyLight, (float) holderSkyLight);
        int packedLight = LightTexture.pack(blockLight, skyLight);
        float shade = piece % 2 == (reverseShade ? 1 : 0) ? 0.7F : 1.0F;
        float red = 0.5F * shade;
        float green = 0.4F * shade;
        float blue = 0.3F * shade;
        float x = deltaX * progress;
        float y = deltaY > 0.0F
                ? deltaY * progress * progress
                : deltaY - deltaY * (1.0F - progress) * (1.0F - progress);
        float z = deltaZ * progress;
        consumer.vertex(pose, x - normalX, y + secondOffset, z + normalZ)
                .color(red, green, blue, 1.0F)
                .uv2(packedLight)
                .endVertex();
        consumer.vertex(pose, x + normalX, y + firstOffset - secondOffset, z - normalZ)
                .color(red, green, blue, 1.0F)
                .uv2(packedLight)
                .endVertex();
    }
}
