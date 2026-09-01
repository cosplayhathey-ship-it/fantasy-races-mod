package com.cosplayhathey.fantasyracesmod.integration;

import com.cosplayhathey.fantasyracesmod.FantasyRacesMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Best-effort auto-detect integration for "Wings"-style mods that provide a ring/item granting glide.
 *
 * Behavior:
 * - Searches the item registry for likely candidate items (common names containing "wing"/"wings"/"ring").
 * - If a candidate is found, it can be given to the player (added to inventory).
 * - The detection is non-invasive and will not error if no such mod is installed.
 */
public class WingsIntegration {
    private static final List<String> CANDIDATE_PATHS = List.of(
            "wing_ring", "wing-ring", "wingring", "wings_ring", "wingsring", "wing", "wings", "ring_wings", "ring_wing", "angel_ring", "flight_ring"
    );

    // Cache the detected item once found
    private static Optional<ResourceLocation> cachedCandidate = null;

    public static boolean isAnyWingsModLoaded() {
        // fast check: look for any loaded mod id that contains "wing"
        try {
            for (var mod : ModList.get().getMods()) {
                String id = mod.getModId();
                if (id != null && id.toLowerCase().contains("wing")) return true;
            }
        } catch (Throwable t) {
            // ignore
        }
        // fallback: see if any matching item exists in registry
        return detectCandidateItem().isPresent();
    }

    public static Optional<ResourceLocation> detectCandidateItem() {
        if (cachedCandidate != null) return cachedCandidate;
        // Try candidate paths across all namespaces
        List<ResourceLocation> found = new ArrayList<>();
        try {
            for (ResourceLocation key : ForgeRegistries.ITEMS.getKeys()) {
                String path = key.getPath().toLowerCase();
                for (String cand : CANDIDATE_PATHS) {
                    if (path.contains(cand)) {
                        found.add(key);
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            FantasyRacesMod.LOGGER.warn("WingsIntegration: failed to scan items for wing candidates", t);
            cachedCandidate = Optional.empty();
            return cachedCandidate;
        }

        // Prefer items from mods whose id contains "wing" first
        Optional<ResourceLocation> preferred = found.stream()
                .filter(k -> k.getNamespace().toLowerCase().contains("wing"))
                .findFirst();
        if (preferred.isPresent()) {
            cachedCandidate = preferred;
            FantasyRacesMod.LOGGER.info("WingsIntegration: detected wings item {} (preferred namespace)", preferred.get());
            return cachedCandidate;
        }

        // Otherwise take the first found
        if (!found.isEmpty()) {
            cachedCandidate = Optional.of(found.get(0));
            FantasyRacesMod.LOGGER.info("WingsIntegration: detected wings-like item {}", cachedCandidate.get());
            return cachedCandidate;
        }

        cachedCandidate = Optional.empty();
        return cachedCandidate;
    }

    public static boolean giveWingsItemToPlayer(Player player) {
        Optional<ResourceLocation> cand = detectCandidateItem();
        if (cand.isEmpty()) return false;
        try {
            Item item = ForgeRegistries.ITEMS.getValue(cand.get());
            if (item == null) return false;
            ItemStack stack = new ItemStack(item);
            boolean added = player.getInventory().add(stack);
            if (!added) {
                // try to drop into world at player position
                player.spawnAtLocation(stack, 0.5F);
            }
            FantasyRacesMod.LOGGER.info("WingsIntegration: gave wings item {} to player {}", cand.get(), player.getName().getString());
            return true;
        } catch (Throwable t) {
            FantasyRacesMod.LOGGER.warn("WingsIntegration: failed to give wings item to player", t);
            return false;
        }
    }
}
