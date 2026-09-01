package com.cosplayhathey.fantasyracesmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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

    // Simple server-side cooldown map to prevent spam (per-player cooldown in ms)
    private static final Map<UUID, Long> lastEmoteAt = new ConcurrentHashMap<>();
    private static final long EMOTE_COOLDOWN_MS = 1000L; // 1 second cooldown

    public static void handle(PlayEmotePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection() != NetworkEvent.Direction.PLAY_TO_SERVER) return;
            ServerPlayer sender = context.getSender();
            if (sender == null) return;
            // Server-side validation: only Neko/catfolk allowed for these emotes (optional)
            // Check cooldown
            UUID id = sender.getUUID();
            long now = System.currentTimeMillis();
            Long last = lastEmoteAt.get(id);
            if (last != null && (now - last) < EMOTE_COOLDOWN_MS) {
                return; // ignore spam
            }
            lastEmoteAt.put(id, now);
            // Broadcast to clients
            NetworkHandler.CHANNEL.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), new PlayEmoteS2CPacket(sender.getUUID(), pkt.type));
        });
        context.setPacketHandled(true);
    }
}
