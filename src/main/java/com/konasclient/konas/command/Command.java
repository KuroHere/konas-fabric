package com.konasclient.konas.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;

import java.util.ArrayList;
import java.util.Collections;

public abstract class Command {

    protected static MinecraftClient mc;

    private final String name;
    private final String description;
    private final ArrayList<String> aliases = new ArrayList<>();

    public Command(String name, String description, String... aliases) {
        this.name = name;
        this.description = description;
        Collections.addAll(this.aliases, aliases);
        mc = MinecraftClient.getInstance();
    }

    protected static <T> RequiredArgumentBuilder<CommandSource, T> argument(final String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected static LiteralArgumentBuilder<CommandSource> literal(final String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public final void registerTo(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.literal(this.name);
        this.build(builder);
        dispatcher.register(builder);

        for (String alias : aliases) {
            LiteralArgumentBuilder<CommandSource> aliasBuilder = LiteralArgumentBuilder.literal(alias);
            this.build(aliasBuilder);
            dispatcher.register(aliasBuilder);
        }
    }

    public abstract void build(LiteralArgumentBuilder<CommandSource> builder);

}