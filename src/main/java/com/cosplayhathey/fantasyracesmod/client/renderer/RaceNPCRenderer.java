package com.cosplayhathey.fantasyracesmod.client.renderer;

import com.cosplayhathey.fantasyracesmod.entity.RaceNPC;
import com.cosplayhathey.fantasyracesmod.skins.RaceSkinManager;
import com.cosplayhathey.fantasyracesmod.common.RaceId;
import com.cosplayhathey.fantasyracesmod.FantasyRacesMod;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class RaceNPCRenderer extends MobRenderer<RaceNPC, PlayerModel<RaceNPC>> {

    private static final ResourceLocation DEFAULT = new ResourceLocation(FantasyRacesMod.MODID, "textures/entity/race_default.png");

    public RaceNPCRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new PlayerModel<>(ctx.bakeLayer(PlayerModel.createBodyLayer())), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(RaceNPC entity) {
        RaceId race = entity.getRace();
        ResourceLocation loc = RaceSkinManager.getTextureForRace(race);
        if (loc == null) return DEFAULT;
        return loc;
    }
}
