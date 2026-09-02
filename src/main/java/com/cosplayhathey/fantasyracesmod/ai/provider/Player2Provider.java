package com.cosplayhathey.fantasyracesmod.ai.provider;

import com.cosplayhathey.fantasyracesmod.ai.provider.AIProvider;
import com.cosplayhathey.fantasyracesmod.FantasyRacesMod;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Player2Provider tries to call into the Player2 mod via reflection.
 *
 * This implementation is best-effort: it checks that a mod with id "player2" is loaded and
 * then attempts to find a reasonable API class and method by well-known names. If it cannot
 * locate a suitable method the provider reports itself unavailable.
 */
public class Player2Provider implements AIProvider {
    private boolean available = false;
    private Object clientApi = null;
    private Method chatMethod = null;

    public Player2Provider() {
        try {
            if (!ModList.get().isLoaded("player2")) return;

            // Try a few common class names that Player2-style mods might expose. This is heuristic-based.
            String[] candidates = new String[] {
                    "dev.player2.api.Player2Api",
                    "player2.api.Player2Api",
                    "com.player2.api.Player2",
                    "player2.Player2",
                    "net.player2.Player2"
            };

            Class<?> apiClass = null;
            for (String name : candidates) {
                try {
                    apiClass = Class.forName(name);
                    break;
                } catch (ClassNotFoundException ignored) {}
            }

            if (apiClass == null) {
                // Try scanning loaded classes via the classloader — expensive; skip for now
                FantasyRacesMod.LOGGER.info("Player2Provider: could not find Player2 API class by common names");
                return;
            }

            // Look for a method that looks like chat/complete (String prompt, Map options) -> String
            for (Method m : apiClass.getMethods()) {
                String mn = m.getName().toLowerCase();
                if (mn.contains("chat") || mn.contains("reply") || mn.contains("complete") || mn.contains("generate")) {
                    Class<?>[] p = m.getParameterTypes();
                    if (p.length >= 1 && p[0].equals(String.class)) {
                        chatMethod = m;
                        break;
                    }
                }
            }

            if (chatMethod == null) {
                FantasyRacesMod.LOGGER.info("Player2Provider: found API class but no suitable chat method");
                return;
            }

            // If method is static we don't need an instance, otherwise try to construct one
            if (!java.lang.reflect.Modifier.isStatic(chatMethod.getModifiers())) {
                try {
                    clientApi = apiClass.getConstructor().newInstance();
                } catch (NoSuchMethodException ns) {
                    try {
                        clientApi = apiClass.getDeclaredConstructor().newInstance();
                    } catch (Throwable t) {
                        FantasyRacesMod.LOGGER.warn("Player2Provider: failed to instantiate Player2 API class", t);
                        return;
                    }
                }
            }

            available = true;
            FantasyRacesMod.LOGGER.info("Player2Provider: Player2 integration available via {}#{}", apiClass.getName(), chatMethod.getName());
        } catch (Throwable t) {
            FantasyRacesMod.LOGGER.warn("Player2Provider: error while initializing provider", t);
            available = false;
        }
    }

    @Override
    public boolean isAvailable() {
        return available && chatMethod != null;
    }

    @Override
    public String getResponse(String npcId, String playerMessage, List<String> history) {
        if (!isAvailable()) return null;
        try {
            // Build a simple prompt combining npcId and history
            StringBuilder prompt = new StringBuilder();
            prompt.append("NPC:").append(npcId).append("\n");
            if (history != null) {
                for (String h : history) {
                    prompt.append(h).append("\n");
                }
            }
            prompt.append("Player: ").append(playerMessage).append("\n");

            Object res;
            if (java.lang.reflect.Modifier.isStatic(chatMethod.getModifiers())) {
                res = chatMethod.invoke(null, prompt.toString());
            } else {
                res = chatMethod.invoke(clientApi, prompt.toString());
            }
            if (res == null) return null;
            return res.toString();
        } catch (Throwable t) {
            FantasyRacesMod.LOGGER.warn("Player2Provider: error calling Player2 API", t);
            return null;
        }
    }
}
