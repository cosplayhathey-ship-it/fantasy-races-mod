package com.cosplayhathey.fantasyracesmod.integration;

import com.cosplayhathey.fantasyracesmod.common.PlayerRaceStorage;
import com.cosplayhathey.fantasyracesmod.common.RaceId;
import com.cosplayhathey.fantasyracesmod.FantasyRacesMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.LogicalSide;

/**
 * Event handlers to auto-distribute wings/rings to bird-type races when they join, respawn, or pick a race.
 * This adds a periodic server-side tick check so that when a player changes their race to a bird, they
 * will receive the wings item shortly after (covers /setrace and similar commands).
 */
@Mod.EventBusSubscriber
public class BirdWingsHandler {
    private static final String PERSIST_TAG = "fr_wings_given";

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getPlayer();
        tryGiveWingsForRace(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        tryGiveWingsForRace(player);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side != LogicalSide.SERVER) return;
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player == null) return;

        // Run check every ~5 seconds (100 ticks)
        if (player.tickCount % 100 != 0) return;

        tryGiveWingsForRace(player);
    }

    private static void tryGiveWingsForRace(Player player) {
        if (player == null || player.level.isClientSide) return;

        RaceId race = PlayerRaceStorage.getRace(player);
        if (!isBirdRace(race)) return;

        CompoundTag data = player.getPersistentData();
        // Player persistent data stores a nested UUID/owner tag; use the root tag for simplicity here.
        boolean already = data.getBoolean(PERSIST_TAG);
        if (already) return;

        // best-effort: if Wings mod item exists, give it to player
        if (WingsIntegration.detectCandidateItem().isPresent()) {
            boolean ok = WingsIntegration.giveWingsItemToPlayer(player);
            if (ok) {
                data.putBoolean(PERSIST_TAG, true);
            } else {
                FantasyRacesMod.LOGGER.warn("BirdWingsHandler: detected wings item but failed to give to player {}", player.getUUID());
            }
        } else {
            FantasyRacesMod.LOGGER.info("BirdWingsHandler: no wings-like item detected; bird race will use fallback glide if implemented");
        }
    }

    private static boolean isBirdRace(RaceId race) {
        if (race == null) return false;
        switch (race) {
            case RAVEN:
            case BAT: // treat bat as a winged race for ring giving
                return true;
            default:
                return false;
        }
    }
}
