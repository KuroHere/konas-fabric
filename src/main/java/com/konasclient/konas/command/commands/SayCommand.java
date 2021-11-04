package com.konasclient.konas.command.commands;

import com.konasclient.konas.command.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class SayCommand extends Command {

    public SayCommand() {
        super("say", "Sends a message to chat.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("message", StringArgumentType.greedyString()).executes(context -> {
            mc.getNetworkHandler().sendPacket(new ChatMessageC2SPacket(StringArgumentType.getString(context, "message")));
            return SINGLE_SUCCESS;
        }));
    }

}
