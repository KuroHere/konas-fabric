package com.konasclient.konas.command.commands;

import com.konasclient.konas.command.Command;
import com.konasclient.konas.command.arguments.ModuleArgumentType;
import com.konasclient.konas.module.Module;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ToggleCommand extends Command {

    public ToggleCommand() {
        super("toggle", "Toggles modules.", "t");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("module", ModuleArgumentType.module())
                .executes(context -> {
                    Module module = ModuleArgumentType.getModule(context, "module");
                    module.toggle();
                    return SINGLE_SUCCESS;
                })
                .then(literal("on")
                        .executes(context -> {
                            Module module = ModuleArgumentType.getModule(context, "module");
                            module.toggle(true);
                            return SINGLE_SUCCESS;
                        })
                ).then(literal("off")
                        .executes(context -> {
                            Module module = ModuleArgumentType.getModule(context, "module");
                            module.toggle(false);
                            return SINGLE_SUCCESS;
                        })
                ));
    }
}
