package com.cosplayhathey.fantasyracesmod.skins;

import com.cosplayhathey.fantasyracesmod.common.RaceId;
import com.cosplayhathey.fantasyracesmod.FantasyRacesMod;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * RaceSkinManager
 *
 * Loads a simple properties file mapping RaceId -> resource location (namespace:path). If no mapping
 * is present for a race, the default texture path will be used: fantasy_races_mod:textures/entity/race_<race>.png
 *
 * Config location: config/fantasy_races_mod/skins.properties
 */
public class RaceSkinManager {
    private static final String CONFIG_DIR = "config/fantasy_races_mod";
    private static final String SKIN_FILE = "skins.properties";

    private static Properties props = new Properties();
    private static boolean loaded = false;

    public static void load() {
        if (loaded) return;
        try {
            Path dir = Path.of(CONFIG_DIR);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Path file = dir.resolve(SKIN_FILE);
            if (!Files.exists(file)) {
                // create an empty properties file with example entries
                props.setProperty("#example", "NEKO=fantasy_races_mod:entity/neko_custom.png");
                try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                    props.store(fos, "Race skin mappings: RACE=namespace:path (e.g., NEKO=fantasy_races_mod:entity/neko.png)");
                }
            } else {
                try (FileInputStream fis = new FileInputStream(file.toFile())) {
                    props.load(fis);
                }
            }
        } catch (IOException e) {
            FantasyRacesMod.LOGGER.warn("RaceSkinManager: failed to create/load skins.properties", e);
        }
        loaded = true;
    }

    public static ResourceLocation getTextureForRace(RaceId race) {
        load();
        if (race == null) return getDefault(null);
        String key = race.name();
        String mapped = props.getProperty(key);
        if (mapped == null || mapped.isBlank()) return getDefault(race);
        // mapped format: namespace:path or namespace:path.png
        if (!mapped.contains(":")) {
            FantasyRacesMod.LOGGER.warn("RaceSkinManager: invalid mapping for {}: {}", key, mapped);
            return getDefault(race);
        }
        String[] parts = mapped.split(":", 2);
        return new ResourceLocation(parts[0], parts[1]);
    }

    private static ResourceLocation getDefault(RaceId race) {
        if (race == null) return new ResourceLocation(FantasyRacesMod.MODID, "textures/entity/race_default.png");
        return new ResourceLocation(FantasyRacesMod.MODID, "textures/entity/race_" + race.name().toLowerCase() + ".png");
    }

    public static boolean setMapping(RaceId race, String resourceLocation) {
        load();
        if (race == null || resourceLocation == null || resourceLocation.isBlank()) return false;
        props.setProperty(race.name(), resourceLocation);
        try {
            Path dir = Path.of(CONFIG_DIR);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Path file = dir.resolve(SKIN_FILE);
            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                props.store(fos, "Race skin mappings: RACE=namespace:path (e.g., NEKO=fantasy_races_mod:entity/neko.png)");
            }
            return true;
        } catch (IOException e) {
            FantasyRacesMod.LOGGER.warn("RaceSkinManager: failed to write skins.properties", e);
            return false;
        }
    }
}
