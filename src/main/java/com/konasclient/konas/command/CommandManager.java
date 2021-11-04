package com.konasclient.konas.command;

import com.konasclient.konas.command.commands.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CommandManager {

    private static final CommandDispatcher<CommandSource> DISPATCHER = new CommandDispatcher<>();
    private static final CommandSource COMMAND_SOURCE = new ChatCommandSource(MinecraftClient.getInstance());
    private static final List<Command> commands = new ArrayList<>();
    private static final Map<Class<? extends Command>, Command> commandInstances = new HashMap<>();

    public static void init() {
        add(new CommandsCommand());
        add(new SayCommand());
        add(new YawCommand());
        add(new PitchCommand());
        add(new ClearCommand());
        add(new FOVCommand());
        add(new ToggleCommand());
        add(new FriendCommand());
        add(new BreadcrumbsCommand());
        add(new SearchCommand());
        add(new BaritoneCommand());
        add(new PeekCommand());
        add(new VClipCommand());
        add(new HClipCommand());
        add(new MacroCommand());
    }

    public static void dispatch(String message) throws CommandSyntaxException {
        dispatch(message, new ChatCommandSource(MinecraftClient.getInstance()));
    }

    public static void dispatch(String message, CommandSource source) throws CommandSyntaxException {
        ParseResults<CommandSource> results = DISPATCHER.parse(message, source);
        CommandManager.DISPATCHER.execute(results);
    }

    public static CommandDispatcher<CommandSource> getDispatcher() {
        return DISPATCHER;
    }

    public static CommandSource getCommandSource() {
        return COMMAND_SOURCE;
    }

    private static void add(Command command) {
        command.registerTo(DISPATCHER);
        commands.add(command);
        commandInstances.put(command.getClass(), command);
    }

    public static int getCount() {
        return commands.size();
    }

    public static void forEach(Consumer<Command> consumer) {
        commands.forEach(consumer);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Command> T get(Class<T> klass) {
        return (T) commandInstances.get(klass);
    }

    private final static class ChatCommandSource extends ClientCommandSource {
        public ChatCommandSource(MinecraftClient client) {
            super(null, client);
        }
    }

}
