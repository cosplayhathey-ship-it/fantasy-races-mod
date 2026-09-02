package com.cosplayhathey.fantasyracesmod.ai;

import java.util.List;

public interface AIProvider {
    boolean isAvailable();
    /**
     * Get a textual response for the given npcId and player message. History is the list of previous messages (player and npc) as simple strings.
     * Returns null if provider cannot produce a response.
     */
    String getResponse(String npcId, String playerMessage, List<String> history);
}
