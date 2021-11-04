package com.konasclient.konas.command.arguments;

import com.konasclient.konas.module.Module;
import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.util.StringUtils;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.LiteralText;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class BindArgumentType implements ArgumentType<Integer> {
    private static final String[] EXAMPLES = new String[]{"b, c, d"};

    private static String[] KEY_NAMES;

    private static final DynamicCommandExceptionType NO_SUCH_KEY = new DynamicCommandExceptionType(o -> new LiteralText("Key with name " + o + " doesn't exist."));

    public static BindArgumentType bind() {
        return new BindArgumentType();
    }

    public static int getBind(final CommandContext<?> context, final String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();
        int bind = StringUtils.getKeyCodeFromKey(argument);

        if (bind == -1) throw NO_SUCH_KEY.create(argument);

        return bind;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (KEY_NAMES == null) KEY_NAMES = StringUtils.getKeyNames();
        return CommandSource.suggestMatching(KEY_NAMES, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return Arrays.asList(EXAMPLES);
    }
}
