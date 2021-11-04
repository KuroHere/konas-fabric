package com.konasclient.konas.command.commands;

import com.konasclient.konas.command.Command;
import com.konasclient.konas.util.chat.Chat;
import com.konasclient.konas.util.friend.Friend;
import com.konasclient.konas.util.friend.Friends;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FriendCommand extends Command {
    public FriendCommand() {
        super("friend", "Manage friends.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("add").then(argument("friend", FriendArgumentType.friend())
                        .executes(context -> {
                            Friend friend = FriendArgumentType.getFriend(context, "friend");

                            if (friend.getName().equalsIgnoreCase(MinecraftClient.getInstance().player.getName().asString())) {
                                Chat.error("You cannot friend yourself!");
                            } else if (Friends.addFriend(friend)) Chat.info("Friended (highlight)%s", friend.getName());
                            else Chat.error("That person is already friended.");

                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        })
                )
        );

        builder.then(literal("del").then(argument("friend", FriendArgumentType.friend())
                        .executes(context -> {
                            Friend friend = FriendArgumentType.getFriend(context, "friend");

                            if (Friends.delFriend(friend)) Chat.info("Unfriended (highlight)%s", friend.getName());
                            else Chat.error( "That person is already unfriended.");

                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        })
                )
        );

        builder.then(literal("list").executes(context -> {
                    Chat.info("Friends:", Friends.friends.size());
                    Friends.friends.forEach(friend-> Chat.info(" - (highlight)%s", friend.getName()));
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                })
        );
    }

    private static class FriendArgumentType implements ArgumentType<Friend> {

        public static FriendArgumentType friend() {
            return new FriendArgumentType();
        }

        @Override
        public Friend parse(StringReader reader) throws CommandSyntaxException {
            return new Friend(reader.readString());
        }

        public static Friend getFriend(CommandContext<?> context, String name) {
            return context.getArgument(name, Friend.class);
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return CommandSource.suggestMatching(MinecraftClient.getInstance().getNetworkHandler().getPlayerList().stream().map(entry -> entry.getProfile().getName()).collect(Collectors.toList()), builder);
        }

        @Override
        public Collection<String> getExamples() {
            return Arrays.asList("Fit", "popbob", "0851_");
        }
    }
}
