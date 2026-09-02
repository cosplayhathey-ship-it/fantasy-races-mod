package com.cosplayhathey.fantasyracesmod.client.layer;

import com.cosplayhathey.fantasyracesmod.common.PlayerRaceStorage;
import com.cosplayhathey.fantasyracesmod.skins.RaceSkinManager;
import com.cosplayhathey.fantasyracesmod.common.RaceId;
import com.cosplayhathey.fantasyracesmod.FantasyRacesMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Layer that renders a full-player skin for race-based appearance using RaceSkinManager.
 * It draws on top of the player's model using the mapped texture.
 */
public class RaceSkinLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public RaceSkinLayer(PlayerRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        try {
            RaceId race = PlayerRaceStorage.getRace(player);
            ResourceLocation tex = RaceSkinManager.getTextureForRace(race);
            if (tex == null) return;

            VertexConsumer vb = buffer.getBuffer(RenderType.entityCutoutNoCull(tex));
            // copy model pose/animations from parent renderer's model
            PlayerModel<AbstractClientPlayer> model = this.getParentModel();
            model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            model.prepareMobModel(player, limbSwing, limbSwingAmount, partialTicks);
            model.renderToBuffer(poseStack, vb, packedLight, LivingEntityRenderer.getOverlayCoords(player, 0f), 1f, 1f, 1f, 1f);
        } catch (Throwable t) {
            FantasyRacesMod.LOGGER.warn("RaceSkinLayer: failed to render race skin for player {}", player.getName().getString(), t);
        }
    }
}
