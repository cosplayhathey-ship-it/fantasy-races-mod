package com.cosplayhathey.fantasyracesmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

import com.cosplayhathey.fantasyracesmod.client.NekoEmoteHandler;
import com.cosplayhathey.fantasyracesmod.FantasyRacesMod;

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

            // Add the emote to the client-side pending map so renderers/Geo layers can pick it up
            NekoEmoteHandler.put(pkt.playerId, pkt.emoteType);

            // Play a sound for the emote locally only for SCRATCH. LICK is intentionally silent.
            if (pkt.emoteType == PlayEmotePacket.EmoteType.SCRATCH) {
                SoundEvent se = ForgeRegistries.SOUND_EVENTS.getValue(new net.minecraft.resources.ResourceLocation(FantasyRacesMod.MODID, "scratch"));
                if (se == null) se = SoundEvents.PLAYER_ATTACK_STRONG;
                if (se != null) {
                    player.playSound(se, 1.0F, 1.0F);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
