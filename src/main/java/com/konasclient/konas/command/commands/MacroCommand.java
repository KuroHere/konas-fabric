package com.konasclient.konas.command.commands;

import com.konasclient.konas.command.Command;
import com.konasclient.konas.command.arguments.BindArgumentType;
import com.konasclient.konas.macro.Macro;
import com.konasclient.konas.macro.MacroManager;
import com.konasclient.konas.util.StringUtils;
import com.konasclient.konas.util.chat.Chat;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

public class MacroCommand extends Command {
    public MacroCommand() {
        super("macro", "Manage macros.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("add")
                        .then(argument("name", StringArgumentType.word())
                        .then(argument("bind", BindArgumentType.bind()))
                        .then(argument("text", StringArgumentType.greedyString()))
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            String text = context.getArgument("text", String.class);
                            int bind = BindArgumentType.getBind(context, "bind");

                            if (MacroManager.getMacroByName(name) != null) {
                                Chat.error("Macro %s already exists!", name);
                            } else {
                                MacroManager.addMacro(new Macro(name, text, bind));
                                Chat.info("Added macro %s!", name);
                            }

                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        })
                )
        );

        builder.then(literal("del").then(argument("name", StringArgumentType.word())
                        .executes(context -> {
                            Macro macro = MacroManager.getMacroByName(StringArgumentType.getString(context, "name"));

                            if (macro != null) {
                                MacroManager.removeMacro(macro);
                                Chat.info("Unadded (highlight)%s(reset) macro", macro.getName());
                            } else {
                                Chat.error( "This macro does not exist or has been already deleted.");
                            }

                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        })
                )
        );

        builder.then(literal("list").executes(context -> {
                    Chat.info("Macros:", MacroManager.getMacros().size());
                    MacroManager.getMacros().forEach(macro-> Chat.info(" - (highlight)%s(reset) %s %s", macro.getName(),  StringUtils.getKeyName(macro.getBind()), macro.getText()));
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                })
        );
    }
}
