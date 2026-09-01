package com.cosplayhathey.fantasyracesmod.client;

import com.cosplayhathey.fantasyracesmod.network.NetworkHandler;
import com.cosplayhathey.fantasyracesmod.network.PlayEmotePacket;
import com.cosplayhathey.fantasyracesmod.common.PlayerRaceStorage;
import com.cosplayhathey.fantasyracesmod.common.RaceId;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
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
            NetworkHandler.CHANNEL.sendToServer(new PlayEmotePacket(PlayEmotePacket.EmoteType.LICK));
        }
        if (SCRATCH_KEY.consumeClick()) {
            NetworkHandler.CHANNEL.sendToServer(new PlayEmotePacket(PlayEmotePacket.EmoteType.SCRATCH));
        }
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseInputEvent event) {
        // Listen for left-click (attack) and trigger scratch emote for Neko/Catfolk when unarmed.
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return; // don't trigger while GUI open
        Player player = mc.player;
        if (player == null) return;

        // Only trigger scratch if player is Neko or Catfolk and is unarmed
        RaceId race = PlayerRaceStorage.getRace(player);
        if (race != RaceId.NEKO && race != RaceId.CATFOLK) return;
        if (!player.getMainHandItem().isEmpty()) return; // only scratch when empty-handed

        // Send scratch emote packet to server; server enforces cooldown and broadcasts to clients
        NetworkHandler.CHANNEL.sendToServer(new PlayEmotePacket(PlayEmotePacket.EmoteType.SCRATCH));
    }
}
