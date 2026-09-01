package com.cosplayhathey.fantasyracesmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class PlayEmotePacket {
    public enum EmoteType { LICK, SCRATCH }
    private final EmoteType type;

    public PlayEmotePacket(EmoteType type) {
        this.type = type;
    }

    public static void encode(PlayEmotePacket pkt, FriendlyByteBuf buf) {
        buf.writeEnum(pkt.type);
    }

    public static PlayEmotePacket decode(FriendlyByteBuf buf) {
        return new PlayEmotePacket(buf.readEnum(EmoteType.class));
    }

    public static void handle(PlayEmotePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection() != NetworkEvent.Direction.PLAY_TO_SERVER) return;
            ServerPlayer sender = context.getSender();
            if (sender == null) return;
            // Validate player race server-side if needed (e.g., only Neko can lick)
            // Broadcast to all clients to play the emote animation
            NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new PlayEmoteS2CPacket(sender.getUUID(), pkt.type));
        });
        context.setPacketHandled(true);
    }
}
