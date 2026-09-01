package com.cosplayhathey.fantasyracesmod.network;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraft.resources.ResourceLocation;

public class NetworkHandler {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("fantasy_races_mod", "network"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int id = 0;

    private static int nextId() {
        return id++;
    }

    public static void register() {
        CHANNEL.registerMessage(nextId(), PlayEmotePacket.class, PlayEmotePacket::encode, PlayEmotePacket::decode, PlayEmotePacket::handle);
        CHANNEL.registerMessage(nextId(), PlayEmoteS2CPacket.class, PlayEmoteS2CPacket::encode, PlayEmoteS2CPacket::decode, PlayEmoteS2CPacket::handle);
    }
}
