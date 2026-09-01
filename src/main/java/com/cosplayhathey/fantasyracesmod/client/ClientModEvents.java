package com.cosplayhathey.fantasyracesmod.client;

import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.settings.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class ClientModEvents {
    public static KeyMapping LICK_KEY;
    public static KeyMapping SCRATCH_KEY;

    public static void registerKeybinds() {
        LICK_KEY = new KeyMapping("key.fantasyracesmod.lick", GLFW.GLFW_KEY_K, "key.categories.fantasyracesmod");
        SCRATCH_KEY = new KeyMapping("key.fantasyracesmod.scratch", GLFW.GLFW_KEY_L, "key.categories.fantasyracesmod");
        ClientRegistry.registerKeyBinding(LICK_KEY);
        ClientRegistry.registerKeyBinding(SCRATCH_KEY);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        // Key handling will be implemented later (send packet to server to trigger animations)
    }
}
