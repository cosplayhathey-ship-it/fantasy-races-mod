package com.cosplayhathey.fantasyracesmod;

import com.cosplayhathey.fantasyracesmod.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FantasyRacesMod.MODID)
public class FantasyRacesMod {
    public static final String MODID = "fantasy_races_mod";

    public FantasyRacesMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        com.cosplayhathey.fantasyracesmod.client.ClientModEvents.registerKeybinds();
        // Register renderer layer via event bus (NekoRenderRegistration will pick it up)
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkHandler.register();
        });
    }
}
