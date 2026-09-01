package com.cosplayhathey.fantasyracesmod.common;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;

public class PlayerRaceStorage {
    private static final String RACE_TAG = "FantasyRacesMod_race";

    public static RaceId getRace(Player player) {
        CompoundTag data = player.getPersistentData();
        if (data.contains(RACE_TAG)) {
            try {
                return RaceId.valueOf(data.getString(RACE_TAG));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    public static void setRace(Player player, RaceId race) {
        CompoundTag data = player.getPersistentData();
        if (race == null) data.remove(RACE_TAG);
        else data.putString(RACE_TAG, race.name());
    }
}
