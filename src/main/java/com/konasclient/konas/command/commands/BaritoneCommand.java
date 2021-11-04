package com.konasclient.konas.command.commands;

import baritone.api.BaritoneAPI;
import com.konasclient.konas.command.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

public class BaritoneCommand extends Command {
    public BaritoneCommand() {
        super("baritone", "Executes baritone commands.", "b");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("arguments", StringArgumentType.greedyString())
                .executes(context -> {
                    String command = context.getArgument("arguments", String.class);
                    BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute(command);
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                }));
    }
}