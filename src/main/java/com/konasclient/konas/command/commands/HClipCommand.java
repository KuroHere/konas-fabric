package com.konasclient.konas.command.commands;

import com.konasclient.konas.command.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.Vec3d;

public class HClipCommand extends Command {
    public HClipCommand() {
        super("hclip", "Clips you forwards", "hc", "clip");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("distance", DoubleArgumentType.doubleArg()).executes(context -> {
            Vec3d clip = Vec3d.fromPolar(0F, mc.player.getYaw(mc.getTickDelta())).normalize().multiply(context.getArgument("distance", Double.class));
            mc.player.updatePosition(mc.player.getX() + clip.x, mc.player.getY(), mc.player.getZ() + clip.z);
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }));
    }
}