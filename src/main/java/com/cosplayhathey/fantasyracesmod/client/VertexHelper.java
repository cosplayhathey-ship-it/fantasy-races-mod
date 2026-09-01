package com.cosplayhathey.fantasyracesmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;

/**
 * Helper methods to draw simple quads for ears/tail as a fallback when Geo rendering is not available.
 */
public class VertexHelper {
    public static void renderEarQuad(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float xOff, float yOff) {
        VertexConsumer builder = bufferSource.getBuffer(RenderType.entityCutoutNoCull(new net.minecraft.resources.ResourceLocation("fantasy_races_mod", "textures/entity/neko.png")));
        poseStack.pushPose();
        poseStack.translate(xOff, yOff, -0.05F);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-10f));
        // draw a simple quad
        PoseStack.Pose last = poseStack.last();
        builder.vertex(last.pose(), -0.06f, 0f, 0.12f).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(last.normal(), 0, 1, 0).endVertex();
        builder.vertex(last.pose(), 0.06f, 0f, 0.12f).uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(last.normal(), 0, 1, 0).endVertex();
        builder.vertex(last.pose(), 0.06f, 0.08f, 0.12f).uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(last.normal(), 0, 1, 0).endVertex();
        builder.vertex(last.pose(), -0.06f, 0.08f, 0.12f).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(last.normal(), 0, 1, 0).endVertex();
        poseStack.popPose();
    }

    public static void renderTailQuad(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        VertexConsumer builder = bufferSource.getBuffer(RenderType.entityCutoutNoCull(new net.minecraft.resources.ResourceLocation("fantasy_races_mod", "textures/entity/neko.png")));
        PoseStack.Pose last = poseStack.last();
        builder.vertex(last.pose(), -0.06f, -0.02f, 0.0f).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(last.normal(), 0, 1, 0).endVertex();
        builder.vertex(last.pose(), 0.06f, -0.02f, 0.0f).uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(last.normal(), 0, 1, 0).endVertex();
        builder.vertex(last.pose(), 0.06f, 0.1f, 0.12f).uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(last.normal(), 0, 1, 0).endVertex();
        builder.vertex(last.pose(), -0.06f, 0.1f, 0.12f).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(last.normal(), 0, 1, 0).endVertex();
    }
}
