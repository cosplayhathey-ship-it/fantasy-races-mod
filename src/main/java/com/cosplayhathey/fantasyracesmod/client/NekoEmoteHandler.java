package com.cosplayhathey.fantasyracesmod.client;

import com.cosplayhathey.fantasyracesmod.network.PlayEmotePacket;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side pending emote storage with expiry handling. Stores emotes broadcast by the server so
 * client renderers can pick them up and play the corresponding Geckolib animations.
 */
public class NekoEmoteHandler {
    private static final Map<UUID, PendingEmote> pending = new ConcurrentHashMap<>();
    private static final long EMOTE_TIMEOUT_MS = 3000L;

    public static void put(UUID playerId, PlayEmotePacket.EmoteType type) {
        pending.put(playerId, new PendingEmote(type, System.currentTimeMillis() + EMOTE_TIMEOUT_MS));
    }

    public static PlayEmotePacket.EmoteType poll(UUID playerId) {
        PendingEmote pe = pending.remove(playerId);
        if (pe == null) return null;
        if (System.currentTimeMillis() > pe.expiry) return null;
        return pe.type;
    }

    public static void cleanup() {
        long now = System.currentTimeMillis();
        pending.entrySet().removeIf(e -> e.getValue().expiry < now);
    }

    private static class PendingEmote {
        final PlayEmotePacket.EmoteType type;
        final long expiry;

        PendingEmote(PlayEmotePacket.EmoteType type, long expiry) {
            this.type = type;
            this.expiry = expiry;
        }
    }
}
