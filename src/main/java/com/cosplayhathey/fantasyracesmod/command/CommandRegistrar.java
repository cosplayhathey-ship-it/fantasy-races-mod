package com.cosplayhathey.fantasyracesmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CommandRegistrar {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher = event.getDispatcher();
        SetRaceCommand.register(dispatcher);
    }
}
