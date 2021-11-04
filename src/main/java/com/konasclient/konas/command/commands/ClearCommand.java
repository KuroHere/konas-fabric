package com.konasclient.konas.command.commands;

import com.konasclient.konas.command.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ClearCommand extends Command {

    public ClearCommand() {
        super("clear", "Clears all messages from chat.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            mc.inGameHud.getChatHud().clear(false);
            return SINGLE_SUCCESS;
        });
    }

}