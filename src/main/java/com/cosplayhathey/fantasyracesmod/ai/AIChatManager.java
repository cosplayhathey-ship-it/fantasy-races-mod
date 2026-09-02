package com.cosplayhathey.fantasyracesmod.ai;

import com.cosplayhathey.fantasyracesmod.FantasyRacesMod;
import com.cosplayhathey.fantasyracesmod.ai.provider.Player2AppProvider;
import com.cosplayhathey.fantasyracesmod.ai.provider.Player2Provider;
import com.cosplayhathey.fantasyracesmod.network.ChatResponsePacket;
import com.cosplayhathey.fantasyracesmod.network.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * AIChatManager: manages per-player/NPC conversation state, cooldowns, and calls providers asynchronously.
 */
public class AIChatManager {
    private static final AIChatManager INSTANCE = new AIChatManager();

    // historyKey: playerUuid +":"+ npcId -> deque of messages (simple alternating player/npc strings)
    private final Map<String, Deque<String>> history = new HashMap<>();

    // cooldowns: playerUuid -> (npcId -> lastMillis)
    private final Map<UUID, Map<Integer, Long>> cooldowns = new HashMap<>();

    private final Player2Provider inJvmProvider = new Player2Provider();
    private final Player2AppProvider appProvider = new Player2AppProvider();

    private AIChatManager() {
    }

    public static AIChatManager getInstance() {
        return INSTANCE;
    }

    private String historyKey(UUID playerUuid, int npcId) {
        return playerUuid.toString() + ":" + npcId;
    }

    private Deque<String> getHistory(UUID playerUuid, int npcId) {
        String k = historyKey(playerUuid, npcId);
        return history.computeIfAbsent(k, _k -> new ArrayDeque<>());
    }

    private boolean isOnCooldown(UUID playerUuid, int npcId, long cooldownMs) {
        Map<Integer, Long> m = cooldowns.get(playerUuid);
        if (m == null) return false;
        Long last = m.get(npcId);
        if (last == null) return false;
        return System.currentTimeMillis() - last < cooldownMs;
    }

    private void setCooldown(UUID playerUuid, int npcId) {
        cooldowns.computeIfAbsent(playerUuid, u -> new HashMap<>()).put(npcId, System.currentTimeMillis());
    }

    private AIProvider chooseProvider() {
        if (inJvmProvider != null && inJvmProvider.isAvailable()) return inJvmProvider;
        if (appProvider != null && appProvider.isAvailable()) return appProvider;
        return null;
    }

    /**
     * Handle a player message to an NPC asynchronously. Sends ChatResponsePacket back to the player when complete.
     */
    public void handleMessageAsync(ServerPlayer player, int npcId, String message) {
        if (player == null) return;
        UUID puid = player.getUUID();
        long cooldownMs = 10_000L; // default 10s; could be loaded from config
        if (isOnCooldown(puid, npcId, cooldownMs)) {
            // Send quick cooldown message
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ChatResponsePacket(npcId, "(Please wait before speaking to that NPC again.)"));
            return;
        }
        setCooldown(puid, npcId);

        Deque<String> hist = getHistory(puid, npcId);
        // Add player message to history
        hist.addLast("Player: " + message);
        while (hist.size() > 20) hist.removeFirst(); // cap at 20

        AIProvider provider = chooseProvider();
        if (provider == null || !provider.isAvailable()) {
            // No provider available; return a simple scripted reply
            String reply = "(The NPC doesn't feel like talking right now.)";
            hist.addLast("NPC: " + reply);
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ChatResponsePacket(npcId, reply));
            return;
        }

        List<String> histList = List.copyOf(hist);
        // Call provider asynchronously
        CompletableFuture.supplyAsync(() -> provider.getResponse(String.valueOf(npcId), message, histList))
                .thenAccept(reply -> {
                    if (reply == null) reply = "(No response.)";
                    hist.addLast("NPC: " + reply);
                    while (hist.size() > 20) hist.removeFirst();
                    NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ChatResponsePacket(npcId, reply));
                })
                .exceptionally(t -> {
                    FantasyRacesMod.LOGGER.warn("AIChatManager: provider error", t);
                    String reply = "(Error getting reply.)";
                    hist.addLast("NPC: " + reply);
                    NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ChatResponsePacket(npcId, reply));
                    return null;
                });
    }
}
