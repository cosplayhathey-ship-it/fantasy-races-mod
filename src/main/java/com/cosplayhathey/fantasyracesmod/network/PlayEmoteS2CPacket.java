package com.cosplayhathey.fantasyracesmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

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
            Player player = (Player) e;
            // TODO: Trigger client-side animation playback for this player (e.g., via Geckolib or a player layer flag)
            // For now we simply print to log
            mc.execute(() -> mc.player.sendMessage(new net.minecraft.network.chat.TextComponent(player.getName().getString() + " performed emote: " + pkt.emoteType), java.util.UUID.randomUUID()));
        });
        ctx.get().setPacketHandled(true);
    }
}
