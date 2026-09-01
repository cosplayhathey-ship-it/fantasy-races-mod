package com.cosplayhathey.fantasyracesmod.client;

import com.cosplayhathey.fantasyracesmod.network.NetworkHandler;
import com.cosplayhathey.fantasyracesmod.network.PlayEmotePacket;
import com.cosplayhathey.fantasyracesmod.network.PlayEmotePacket.EmoteType;
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
        // Note: InputEvent.Key is fired for raw key presses; to safely detect key press we should poll KeyMapping.isDown in client tick.
    }

    @SubscribeEvent
    public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        if (LICK_KEY.consumeClick()) {
            NetworkHandler.CHANNEL.sendToServer(new PlayEmotePacket(EmoteType.LICK));
        }
        if (SCRATCH_KEY.consumeClick()) {
            NetworkHandler.CHANNEL.sendToServer(new PlayEmotePacket(EmoteType.SCRATCH));
        }
    }
}
