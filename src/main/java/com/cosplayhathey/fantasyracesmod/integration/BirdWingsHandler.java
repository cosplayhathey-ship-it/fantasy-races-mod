package com.cosplayhathey.fantasyracesmod.integration;

import com.cosplayhathey.fantasyracesmod.common.PlayerRaceStorage;
import com.cosplayhathey.fantasyracesmod.common.RaceId;
import com.cosplayhathey.fantasyracesmod.FantasyRacesMod;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handlers to auto-distribute wings/rings to bird-type races when they join or respawn.
 */
@Mod.EventBusSubscriber
public class BirdWingsHandler {

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

    private static void tryGiveWingsForRace(Player player) {
        if (player == null || player.level.isClientSide) return;
        RaceId race = PlayerRaceStorage.getRace(player);
        if (race == RaceId.RAVEN || race == RaceId.BAT) {
            // best-effort: if Wings mod item exists, give it to player
            if (WingsIntegration.detectCandidateItem().isPresent()) {
                boolean ok = WingsIntegration.giveWingsItemToPlayer(player);
                if (!ok) {
                    FantasyRacesMod.LOGGER.warn("BirdWingsHandler: detected wings item but failed to give to player {}", player.getUUID());
                }
            } else {
                FantasyRacesMod.LOGGER.info("BirdWingsHandler: no wings-like item detected; Raven/Bat will use fallback glide if implemented");
            }
        }
    }
}
