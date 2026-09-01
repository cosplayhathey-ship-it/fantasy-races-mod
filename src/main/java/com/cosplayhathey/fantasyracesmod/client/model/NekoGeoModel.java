package com.cosplayhathey.fantasyracesmod.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class NekoGeoModel extends AnimatedGeoModel<Player> {
    @Override
    public ResourceLocation getModelLocation(Player object) {
        return new ResourceLocation("fantasy_races_mod", "models/geckolib/neko.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(Player object) {
        return new ResourceLocation("fantasy_races_mod", "textures/entity/neko.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(Player animatable) {
        return new ResourceLocation("fantasy_races_mod", "animations/neko.animation.json");
    }
}
