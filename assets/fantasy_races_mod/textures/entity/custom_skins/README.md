# Custom Skins Folder

Place custom race skin PNG files in this directory to include them in the mod build.

Conventions
- Path inside the mod JAR: assets/fantasy_races_mod/textures/entity/custom_skins/
- Filename recommendation: race_<race>.png (e.g., race_neko.png, race_raven.png)
- When you add files here and build the mod, reference them in config/fantasy_races_mod/skins.properties using the resource location:
  fantasy_races_mod:entity/custom_skins/<filename>
  Example:
    NEKO=fantasy_races_mod:entity/custom_skins/race_neko.png

Notes
- For multiplayer, clients must have the same resource available (via the mod JAR or a resource pack) at that resource location to see the custom skin.
- If you want server-side runtime skin distribution, let me know and I can add an optional dynamic-skin system, but it requires a client-side component and explicit consent from players.

How to add
1. Add the PNG files into this directory in the repository and rebuild the mod (or include them in a resource pack for clients).
2. Edit config/fantasy_races_mod/skins.properties to map RaceId to the resource location.
3. Restart the server/client (or reload resource packs) and spawn RaceNPCs to see the new skins.
