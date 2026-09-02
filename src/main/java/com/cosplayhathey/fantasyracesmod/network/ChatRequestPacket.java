package com.cosplayhathey.fantasyracesmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChatRequestPacket {
    private final int npcId;
    private final String message;

    public ChatRequestPacket(int npcId, String message) {
        this.npcId = npcId;
        this.message = message;
    }

    public static void encode(ChatRequestPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.npcId);
        buf.writeUtf(pkt.message == null ? "" : pkt.message);
    }

    public static ChatRequestPacket decode(FriendlyByteBuf buf) {
        int id = buf.readInt();
        String m = buf.readUtf(32767);
        return new ChatRequestPacket(id, m);
    }

    public static void handle(ChatRequestPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context ctx0 = ctx.get();
        ctx0.enqueueWork(() -> {
            if (ctx0.getDirection() != NetworkEvent.Direction.PLAY_TO_SERVER) return;
            net.minecraft.server.level.ServerPlayer sender = ctx0.getSender();
            if (sender == null) return;
            // Pass to AIChatManager
            com.cosplayhathey.fantasyracesmod.ai.AIChatManager.getInstance().handleMessageAsync(sender, pkt.npcId, pkt.message);
        });
        ctx0.setPacketHandled(true);
    }
}
