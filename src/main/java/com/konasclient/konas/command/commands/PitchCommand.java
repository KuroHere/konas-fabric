package com.konasclient.konas.command.commands;

import com.konasclient.konas.command.Command;
import com.konasclient.konas.util.chat.Chat;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class PitchCommand extends Command {

    public PitchCommand() {
        super("pitch", "Sets your pitch.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("pitch", FloatArgumentType.floatArg(-90, 90))
                .executes(context -> {
                    float pitch = FloatArgumentType.getFloat(context, "pitch");

                    mc.player.pitch = pitch;
                    Chat.info("Set pitch to: (blue)%s", pitch);

                    return SINGLE_SUCCESS;
                })
        );
    }
}
