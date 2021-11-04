package com.konasclient.konas.command.commands;

import com.konasclient.konas.command.Command;
import com.konasclient.konas.command.CommandManager;
import com.konasclient.konas.util.chat.Chat;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class CommandsCommand extends Command {

    public CommandsCommand() {
        super("commands", "Outputs a list of all available commands.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Chat.info("(blue)Commands" + " (reset)(%s)", CommandManager.getCount());
            Chat.lineBreak(true);
            CommandManager.forEach(command -> Chat.info("(blue)%s(reset) -> %s", command.getName(), command.getDescription()));
            return SINGLE_SUCCESS;
        });
    }

}