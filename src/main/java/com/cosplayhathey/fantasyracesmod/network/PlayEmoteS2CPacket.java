package com.cosplayhathey.fantasyracesmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

import com.cosplayhathey.fantasyracesmod.client.NekoGeoLayer;

public class PlayEmoteS2CPacket {
    private final UUID playerId;
    private final PlayEmotePacket.EmoteType emoteType;

    public PlayEmoteS2CPacket(UUID playerId, PlayEmotePacket.EmoteType emoteType) {
        this.playerId = playerId;
        this.emoteType = emoteType;
    }

    public static void encode(PlayEmoteS2CPacket pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.playerId);
        buf.writeEnum(pkt.emoteType);
    }

    public static PlayEmoteS2CPacket decode(FriendlyByteBuf buf) {
        return new PlayEmoteS2CPacket(buf.readUUID(), buf.readEnum(PlayEmotePacket.EmoteType.class));
    }

    public static void handle(PlayEmoteS2CPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            Entity e = mc.level.getEntity(pkt.playerId);
            if (!(e instanceof Player)) return;
            // Instead of printing a chat message, add the emote to the pending map so the NekoGeoLayer can pick it up
            NekoGeoLayer.pendingEmotes.put(pkt.playerId, pkt.emoteType);
        });
        ctx.get().setPacketHandled(true);
    }
}
