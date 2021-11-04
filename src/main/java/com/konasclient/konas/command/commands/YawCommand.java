package com.konasclient.konas.command.commands;

import com.konasclient.konas.command.Command;
import com.konasclient.konas.util.chat.Chat;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class YawCommand extends Command {

    public YawCommand() {
        super("yaw", "Sets your yaw.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("yaw", FloatArgumentType.floatArg(-360, 360))
                .executes(context -> {
                    float yaw = FloatArgumentType.getFloat(context, "yaw");

                    mc.player.yaw = yaw;
                    Chat.info("Set yaw to: (blue)%s", yaw);

                    return SINGLE_SUCCESS;
                })
        );
    }

}
