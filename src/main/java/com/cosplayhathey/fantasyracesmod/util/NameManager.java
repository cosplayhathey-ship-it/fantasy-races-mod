package com.cosplayhathey.fantasyracesmod.util;

import com.cosplayhathey.fantasyracesmod.common.RaceId;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Simple NameManager that provides random fantasy-style names per race.
 * Uses hard-coded lists but can be extended to load from resources later.
 */
public class NameManager {
    private static final NameManager INSTANCE = new NameManager();
    private final Map<RaceId, String[]> names = new HashMap<>();
    private final Random random = new Random();

    private NameManager() {
        // populate default name lists per race id where appropriate
        // Add reasonable defaults; missing races fall back to human names
        names.put(RaceId.HUMAN, new String[]{"Alden","Brynn","Cedric","Daria","Eldon","Fiora","Garrick","Helena","Ilyan","Jora","Kael","Lysa","Merrin","Nora","Orin","Perrin","Quint","Raina","Soren","Thalia"});
        names.put(RaceId.ELF, new String[]{"Aerendyl","Elaria","Faelor","Galanthir","Ithilwen","Lorien","Maelwen","Naeris","Olorin","Phalara","Quelanis","Ryloth","Sylvar","Taelith","Vaelora","Yavanni"});
        names.put(RaceId.DWARF, new String[]{"Borin","Durgan","Fargrim","Grimnar","Haldor","Krag","Marnix","Nori","Oskar","Rurik","Thorli","Ulfgar","Varric"});
        names.put(RaceId.ORC, new String[]{"Goruk","Hruk","Kragth","Mogru","Nokgar","Othuk","Ragthar","Thog","Urzog"});
        names.put(RaceId.NEKO, new String[]{"Mimi","Neko","Kiki","Mika","Sora","Yumi","Tora","Luna","Momo","Nari"});
        names.put(RaceId.CATFOLK, new String[]{"Whisker","Pounce","Felicity","Clawdia","Purrin","Mittens","Sable","Catrin","Tabbi","Syr"});
        names.put(RaceId.GNOME, new String[]{"Bibble","Caskin","Dipple","Fizzle","Gimble","Hobb","Izzit","Jingle","Kipper","Mocket"});
        names.put(RaceId.HALFLING, new String[]{"Bilby","Cally","Doran","Errin","Finnan","Gwyn","Heddie","Imri","Joss","Keth"});
        // fallback list
        names.put(null, new String[]{"Alden","Brynn","Cedric","Daria","Eldon","Fiora","Garrick","Helena"});
    }

    public static NameManager getInstance() {
        return INSTANCE;
    }

    public String getRandomName(RaceId race) {
        String[] arr = names.get(race);
        if (arr == null) arr = names.get(null);
        return arr[random.nextInt(arr.length)];
    }

    // Allow registering custom lists at runtime
    public void registerNames(RaceId race, String[] list) {
        if (race == null) return;
        names.put(race, list);
    }
}
