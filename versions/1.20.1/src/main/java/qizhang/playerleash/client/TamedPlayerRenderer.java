package qizhang.playerleash.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import qizhang.playerleash.QizhangPlayerLeash;

public final class TamedPlayerRenderer {
    private static final Map<UUID, Wolf> WOLVES = new HashMap<>();
    private static ClientLevel cachedLevel;

    private TamedPlayerRenderer() {
    }

    public static boolean renderIfTamed(
            AbstractClientPlayer player,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        MobEffectInstance effect = player.getEffect(QizhangPlayerLeash.tamedEffect());
        if (effect == null || effect.getAmplifier() < 2 || player.isInvisible()) {
            return false;
        }
        if (!(player.level() instanceof ClientLevel level)) {
            return false;
        }
        if (cachedLevel != level) {
            cachedLevel = level;
            WOLVES.clear();
        }

        Wolf wolf = WOLVES.computeIfAbsent(player.getUUID(), ignored -> {
            Wolf created = new Wolf(EntityType.WOLF, level);
            created.setTame(true);
            created.setOwnerUUID(player.getUUID());
            return created;
        });
        copyVisualState(player, wolf);

        EntityRenderer<? super Wolf> renderer = Minecraft.getInstance()
                .getEntityRenderDispatcher()
                .getRenderer(wolf);
        renderer.render(wolf, entityYaw, partialTicks, poseStack, buffers, packedLight);
        return true;
    }

    private static void copyVisualState(AbstractClientPlayer player, Wolf wolf) {
        wolf.setPos(player.getX(), player.getY(), player.getZ());
        wolf.xo = player.xo;
        wolf.yo = player.yo;
        wolf.zo = player.zo;
        wolf.xOld = player.xOld;
        wolf.yOld = player.yOld;
        wolf.zOld = player.zOld;
        wolf.setYRot(player.getYRot());
        wolf.yRotO = player.yRotO;
        wolf.setXRot(player.getXRot());
        wolf.xRotO = player.xRotO;
        wolf.yBodyRot = player.yBodyRot;
        wolf.yBodyRotO = player.yBodyRotO;
        wolf.yHeadRot = player.yHeadRot;
        wolf.yHeadRotO = player.yHeadRotO;
        wolf.tickCount = player.tickCount;
        wolf.setInSittingPose(player.isCrouching());
        wolf.setCustomName(player.getDisplayName());
        wolf.setCustomNameVisible(true);
    }
}
