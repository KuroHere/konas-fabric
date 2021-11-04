package com.konasclient.konas.command.commands;

import com.google.common.io.Files;
import com.konasclient.konas.command.Command;
import com.konasclient.konas.module.modules.render.Breadcrumbs;
import com.konasclient.konas.util.chat.Chat;
import com.konasclient.konas.util.config.Config;
import com.konasclient.konas.util.friend.Friend;
import com.konasclient.konas.util.friend.Friends;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.Vec3d;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BreadcrumbsCommand extends Command {
    public static final File BREADCRUMBS = new File(Config.KONAS_FOLDER, "breadcrumbs");

    public BreadcrumbsCommand() {
        super("breadcrumbs", "Save and load breadcrumbs.");
        if (!BREADCRUMBS.exists()) BREADCRUMBS.mkdir();
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("save").then(argument("name", StringArgumentType.word())
                        .executes(context -> {
                            try {
                                FileWriter csvWriter = new FileWriter(BREADCRUMBS + File.separator + StringArgumentType.getString(context, "name") + ".csv");
                                for (Vec3d vertex : Breadcrumbs.vertices) {
                                    csvWriter.append(vertex.x + "," + vertex.y + "," + vertex.z);
                                    csvWriter.append("\n");
                                }
                                csvWriter.flush();
                                csvWriter.close();
                                Chat.info("Saved breadcrumbs!");
                            } catch (IOException e) {
                                Chat.error("Error while saving breadcrumbs!");
                            }

                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        })
                )
        );

        builder.then(literal("load").then(argument("name", StringArgumentType.word())
                        .executes(context -> {
                            File csvFile = new File(BREADCRUMBS + File.separator + StringArgumentType.getString(context, "name") + ".csv");
                            if (csvFile.isFile()) {
                                try {
                                    BufferedReader csvReader = new BufferedReader(new FileReader(BREADCRUMBS + File.separator + StringArgumentType.getString(context, "name") + ".csv"));
                                    Breadcrumbs.vertices.clear();
                                    String row;
                                    while ((row = csvReader.readLine()) != null) {
                                        String[] data = row.split(",");
                                        Vec3d vertex = new Vec3d(Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]));
                                        Breadcrumbs.vertices.add(vertex);
                                    }
                                    csvReader.close();
                                    Breadcrumbs.onlyRender.setValue(true);
                                    Chat.info("Loaded breadcrumbs!");
                                } catch (Exception e) {
                                    Chat.error("Error while loading breadcrumbs, please ensure your file is not corrupted!");
                                }
                            } else {
                                Chat.error("Invalid filename!");
                            }

                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        })
                )
        );

        builder.then(literal("list").executes(context -> {
                    Chat.info("Saved Breadcrumbs:");
                    if (BREADCRUMBS.listFiles() != null) {
                        List<File> files = Arrays.stream(BREADCRUMBS.listFiles()).filter(f -> f.getName().endsWith(".csv")).collect(Collectors.toList());
                        files.forEach(file -> {
                            Chat.info(" - (highlight)%s", Files.getNameWithoutExtension(file.getName()));
                        });
                    }
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                })
        );

        builder.then(literal("clear").executes(context -> {
            Breadcrumbs.vertices.clear();
            Chat.info("Cleared Breadcrumbs");
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }));
    }
}
