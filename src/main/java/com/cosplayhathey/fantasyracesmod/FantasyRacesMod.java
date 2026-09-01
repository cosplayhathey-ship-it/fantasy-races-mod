package com.cosplayhathey.fantasyracesmod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FantasyRacesMod.MODID)
public class FantasyRacesMod {
    public static final String MODID = "fantasyracesmod";

    public FantasyRacesMod() {
        // Register mod event bus listeners
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);

        // Register to Forge event bus
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        // Client-side setup (keybinds, renderers) will go here
        com.cosplayhathey.fantasyracesmod.client.ClientModEvents.registerKeybinds();
    }
}
