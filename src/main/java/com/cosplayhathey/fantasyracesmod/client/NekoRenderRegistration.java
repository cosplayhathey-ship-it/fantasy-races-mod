package com.cosplayhathey.fantasyracesmod.client;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Note: This class is a placeholder hook where you can register Geo renderers or player layers.
// To actually use Geckolib, add the Geckolib dependency to build.gradle and implement a GeoEntityRenderer
// or a layer that attaches the Geo model to the player renderer.

@Mod.EventBusSubscriber(value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class NekoRenderRegistration {

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.AddLayers event) {
        // Placeholder: register player layer here once Geckolib is available.
        // Example (pseudocode):
        // PlayerRenderer renderer = event.getRenderer(Player.class);
        // renderer.addLayer(new NekoGeoLayer(renderer));
    }
}
