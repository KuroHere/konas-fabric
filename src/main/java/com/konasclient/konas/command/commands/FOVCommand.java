package com.konasclient.konas.command.commands;

import com.konasclient.konas.command.Command;
import com.konasclient.konas.util.chat.Chat;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class FOVCommand extends Command {

    public FOVCommand() {
        super("fov", "Sets your FOV.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("value", DoubleArgumentType.doubleArg(0, 170)).executes(context -> {

            double fov = DoubleArgumentType.getDouble(context, "value");

            mc.options.fov = fov;
            Chat.info("Set fov to: (blue)%s", fov);

            return SINGLE_SUCCESS;
        }));
    }

}
