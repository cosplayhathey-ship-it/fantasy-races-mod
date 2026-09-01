package com.cosplayhathey.fantasyracesmod.client;

import com.cosplayhathey.fantasyracesmod.common.PlayerRaceStorage;
import com.cosplayhathey.fantasyracesmod.common.RaceId;
import com.cosplayhathey.fantasyracesmod.network.NetworkHandler;
import com.cosplayhathey.fantasyracesmod.network.PlayEmotePacket;
import com.cosplayhathey.fantasyracesmod.network.PlayEmotePacket.EmoteType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * NekoLayer renders simple ears and a tail attached to the player's model head/waist.
 * This is a vanilla-rendered placeholder. It uses the player model's head transform
 * so ears follow the player's head rotation. Replace with a Geckolib Geo layer later
 * for richer animated models.
 */
@OnlyIn(Dist.CLIENT)
public class NekoLayer extends RenderLayer<Player, PlayerModel<Player>> {
    private static final ResourceLocation NEKO_TEXTURE = new ResourceLocation("fantasy_races_mod", "textures/entity/neko.png");

    public NekoLayer(RenderLayerParent<Player, PlayerModel<Player>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Player player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        RaceId race = PlayerRaceStorage.getRace(player);
        if (race != RaceId.NEKO && race != RaceId.CATFOLK)
            return;

        // Prepare to render attached to head
        poseStack.pushPose();
        // Align with player's head
        this.getParentModel().head.translateAndRotate(poseStack);

        VertexConsumer builder = bufferSource.getBuffer(RenderType.entityCutoutNoCull(NEKO_TEXTURE));

        // Render left ear quad
        poseStack.pushPose();
        poseStack.translate(-0.22F, -0.18F, -0.05F); // offset relative to head pivot
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-10f));
        renderQuad(poseStack, builder, packedLight, -0.06f, 0f, 0.12f, 0.08f, 0f, 0.08f);
        poseStack.popPose();

        // Render right ear quad (mirror on X)
        poseStack.pushPose();
        poseStack.translate(0.22F, -0.18F, -0.05F);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-10f));
        renderQuad(poseStack, builder, packedLight, -0.06f, 0f, 0.12f, 0.08f, 0f, 0.08f);
        poseStack.popPose();

        // Render tail attached near waist - first transform back out of head space
        poseStack.popPose();
        poseStack.pushPose();
        // Translate to roughly player's waist position
        poseStack.translate(0.0F, 0.45F, 0.18F);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(30f));
        renderQuad(poseStack, builder, packedLight, -0.06f, -0.02f, 0.0f, 0.12f, 0.02f, 0.12f);
        poseStack.popPose();
    }

    private void renderQuad(PoseStack poseStack, VertexConsumer builder, int packedLight, float x, float y, float z, float width, float height, float depth) {
        // Build a simple rectangular prism (thin) by drawing 6 quads (front/back/sides). For simplicity we draw two quads (front + back)
        PoseStack.Pose last = poseStack.last();
        float u0 = 0f, v0 = 0f, u1 = 16f/64f, v1 = 16f/64f; // uses portion of texture; texture UVs assumed
        // Front face
        builder.vertex(last.pose(), x, y + height, z + depth).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(last.normal(), 0, 1, 0).endVertex();
        builder.vertex(last.pose(), x + width, y + height, z + depth).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(last.normal(), 0, 1, 0).endVertex();
        builder.vertex(last.pose(), x + width, y, z + depth).uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(last.normal(), 0, 1, 0).endVertex();
        builder.vertex(last.pose(), x, y, z + depth).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(last.normal(), 0, 1, 0).endVertex();
        // Back face
        builder.vertex(last.pose(), x, y + height, z).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(last.normal(), 0, 1, 0).endVertex();
        builder.vertex(last.pose(), x + width, y + height, z).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(last.normal(), 0, 1, 0).endVertex();
        builder.vertex(last.pose(), x + width, y, z).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(last.normal(), 0, 1, 0).endVertex();
        builder.vertex(last.pose(), x, y, z).uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(last.normal(), 0, 1, 0).endVertex();
    }
}
