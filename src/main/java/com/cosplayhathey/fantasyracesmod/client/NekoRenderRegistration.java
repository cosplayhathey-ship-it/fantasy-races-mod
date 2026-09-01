package com.cosplayhathey.fantasyracesmod.client;

import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.player.PlayerModel;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class NekoRenderRegistration {

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.AddLayers event) {
        // Register NekoGeoLayer for player renderer
        PlayerRenderer renderer = event.getRenderer(net.minecraft.world.entity.player.Player.class);
        if (renderer != null) {
            renderer.addLayer(new NekoGeoLayer((RenderLayerParent) renderer));
        }
    }
}
