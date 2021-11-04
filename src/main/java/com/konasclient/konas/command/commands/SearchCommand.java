package com.konasclient.konas.command.commands;

import com.konasclient.konas.command.Command;
import com.konasclient.konas.module.modules.render.Search;
import com.konasclient.konas.util.chat.Chat;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

public class SearchCommand extends Command {

    public SearchCommand() {
        super("search", "Add and remove blocks from search.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("add").then(argument("name", StringArgumentType.greedyString())
                        .executes(context -> {

                            if(Search.filter.getValue().addBlock(StringArgumentType.getString(context, "name"))) {
                                Chat.info("Added Block " + StringArgumentType.getString(context, "name"));
                                if (mc.worldRenderer != null) {
                                    mc.worldRenderer.reload();
                                }
                            } else {
                                Chat.error("Couldn't find block " + StringArgumentType.getString(context, "name"));
                            }

                            Search.filter.getValue().refreshBlocks();

                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        })
                )
        );

        builder.then(literal("del").then(argument("name", StringArgumentType.greedyString())
                        .executes(context -> {
                            if(Search.filter.getValue().removeBlock(StringArgumentType.getString(context, "name"))) {
                                Chat.info("Removed Block " + StringArgumentType.getString(context, "name"));
                                if (mc.worldRenderer != null) {
                                    mc.worldRenderer.reload();
                                }
                            } else {
                                Chat.error("Couldn't find block " + StringArgumentType.getString(context, "name"));
                            }

                            Search.filter.getValue().refreshBlocks();

                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        })
                )
        );

        builder.then(literal("list").executes(context -> {
                int id = 4444;
                Chat.info("Search Blocks:");
                for (String name : Search.filter.getValue().getBlocksAsString()) {
                        Chat.info(id, " - (highlight)%s", name);
                        id++;
                }
                return com.mojang.brigadier.Command.SINGLE_SUCCESS;
            })
        );
    }
}

