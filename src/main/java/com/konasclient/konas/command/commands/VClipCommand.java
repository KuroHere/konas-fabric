package com.konasclient.konas.command.commands;

import com.konasclient.konas.command.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

public class VClipCommand extends Command {
    public VClipCommand() {
        super("vclip", "Clips you forwards", "vc");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("distance", DoubleArgumentType.doubleArg()).executes(context -> {
            mc.player.updatePosition(mc.player.getX(), mc.player.getY() + context.getArgument("distance", Double.class), mc.player.getZ());
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }));
    }
}
