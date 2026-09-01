package com.cosplayhathey.fantasyracesmod.command;

import com.cosplayhathey.fantasyracesmod.common.PlayerRaceStorage;
import com.cosplayhathey.fantasyracesmod.common.RaceId;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class SetRaceCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("setrace")
            .then(Commands.argument("race", StringArgumentType.word())
                .executes(context -> {
                    String raceName = StringArgumentType.getString(context, "race");
                    CommandSourceStack source = context.getSource();
                    try {
                        RaceId race = RaceId.valueOf(raceName.toUpperCase());
                        ServerPlayer player = source.getPlayerOrException();
                        PlayerRaceStorage.setRace(player, race);
                        source.sendSuccess(net.minecraft.network.chat.Component.literal("Set race to " + race.name()), true);
                        return 1;
                    } catch (IllegalArgumentException e) {
                        source.sendFailure(net.minecraft.network.chat.Component.literal("Unknown race: " + raceName));
                        return 0;
                    }
                })
            )
        );
    }
}
