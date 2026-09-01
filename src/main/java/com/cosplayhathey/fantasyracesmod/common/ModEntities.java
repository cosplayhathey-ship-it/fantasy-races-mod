package com.cosplayhathey.fantasyracesmod.common;

import com.cosplayhathey.fantasyracesmod.FantasyRacesMod;
import com.cosplayhathey.fantasyracesmod.entity.RaceNPC;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, FantasyRacesMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FantasyRacesMod.MODID);

    public static final RegistryObject<EntityType<RaceNPC>> RACE_NPC = ENTITIES.register("race_npc",
            () -> EntityType.Builder.of(RaceNPC::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .build(new ResourceLocation(FantasyRacesMod.MODID, "race_npc").toString())
    );

    public static final RegistryObject<Item> RACE_NPC_SPAWN_EGG = ITEMS.register("race_npc_spawn_egg",
            () -> new SpawnEggItem(() -> RACE_NPC.get(), 0x99ccff, 0x663300, new Item.Properties().tab(CreativeModeTab.TAB_MISC))
    );

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        ITEMS.register(bus);
    }
}
