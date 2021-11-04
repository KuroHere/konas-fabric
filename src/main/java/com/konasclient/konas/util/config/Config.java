package com.konasclient.konas.util.config;

import com.google.gson.*;
import com.konasclient.konas.macro.Macro;
import com.konasclient.konas.macro.MacroManager;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.setting.*;
import com.konasclient.konas.util.friend.Friend;
import com.konasclient.konas.util.friend.Friends;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Config {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static final File KONAS_FOLDER = new File(mc.runDirectory, "Konas");

    public static final File CONFIG = new File(KONAS_FOLDER, "config.json");
    public static final File CONFIGS = new File(KONAS_FOLDER, "configs");

    public static final File ACCOUNTS = new File(KONAS_FOLDER, "accounts.json");

    public static File currentConfig = CONFIG;

    public static void load(File config) {
        if (!config.exists()) {
            System.err.println("Unable to load config as the file does not exist!");
            return;
        }

        try {
            FileReader reader = new FileReader(config);
            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject) parser.parse(reader);

            if (object.has("Modules")) {
                JsonObject modulesObject = (JsonObject) object.get("Modules");
                for (Module module : ModuleManager.getModules()) {
                    if (modulesObject.has(module.getName())) {
                        JsonObject moduleObject = (JsonObject) modulesObject.get(module.getName());
                        try {
                            // Bind
                            int bind = moduleObject.getAsJsonPrimitive("Bind").getAsInt();
                            module.setKeybind(bind);

                            // Enabled
                            boolean enabled = moduleObject.getAsJsonPrimitive("Enabled").getAsBoolean();
                            module.toggle(enabled);

                            // Visible
                            boolean visible = moduleObject.getAsJsonPrimitive("Visible").getAsBoolean();
                            module.setVisible(visible);

                            boolean hold = moduleObject.getAsJsonPrimitive("Hold").getAsBoolean();
                            module.setHold(hold);
                        } catch (Exception ignored) {

                        }
                        for (Setting s : module.getSettingList()) {
                            if (moduleObject.has(s.getName())) {
                                try {
                                    if (s.getValue() instanceof Float) {
                                        s.setValue(moduleObject.getAsJsonPrimitive(s.getName()).getAsFloat());
                                    } else if (s.getValue() instanceof Double) {
                                        s.setValue(moduleObject.getAsJsonPrimitive(s.getName()).getAsDouble());
                                    } else if (s.getValue() instanceof Integer) {
                                        s.setValue(moduleObject.getAsJsonPrimitive(s.getName()).getAsInt());
                                    } else if (s.getValue() instanceof Boolean) {
                                        s.setValue(moduleObject.getAsJsonPrimitive(s.getName()).getAsBoolean());
                                    } else if (s.getValue() instanceof Parent) {
                                        ((Parent) s.getValue()).setExtended(moduleObject.getAsJsonPrimitive(s.getName()).getAsBoolean());
                                    } else if (s.getValue() instanceof Enum) {
                                        s.setEnumValue(moduleObject.getAsJsonPrimitive(s.getName()).getAsString());
                                    } else if (s.getValue() instanceof String) {
                                        s.setValue(moduleObject.getAsJsonPrimitive(s.getName()).getAsString());
                                    } else if (s.getValue() instanceof ColorSetting) {
                                        JsonArray array = moduleObject.getAsJsonArray(s.getName());
                                        ((ColorSetting) s.getValue()).setColor(array.get(0).getAsInt());
                                        ((ColorSetting) s.getValue()).setCycle(array.get(1).getAsBoolean());
                                    } else if (s.getValue() instanceof BlockListSetting) {
                                        JsonArray array = moduleObject.getAsJsonArray(s.getName());
                                        array.forEach(jsonElement -> {
                                            String str = jsonElement.getAsString();
                                            ((BlockListSetting) s.getValue()).addBlock(str);
                                        });
                                        ((BlockListSetting) s.getValue()).refreshBlocks();
                                    } else if (s.getValue() instanceof SubBind) {
                                        s.setValue(new SubBind(moduleObject.getAsJsonPrimitive(s.getName()).getAsInt()));
                                    }
                                } catch (Exception e) {

                                }
                            }
                        }
                    }
                }
            }

            if (object.has("Friends")) {
                JsonArray friends = (JsonArray) object.get("Friends");
                friends.forEach(jsonElement -> {
                    Friends.addFriend(new Friend(jsonElement.getAsString()));
                });
            }

            if (object.has("Macros")) {
                JsonArray macros = object.getAsJsonArray("Macros");
                MacroManager.clearMacros();
                macros.forEach(m -> {
                    try {
                        parseMacro(m.getAsJsonObject());
                    } catch (NullPointerException ignored) {

                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading config");
        }
    }

    private static void parseMacro(JsonObject macro) {
        MacroManager.addMacro(new Macro(macro.get("Name").getAsString(), macro.get("Text").getAsString(), macro.get("Bind").getAsInt()));
    }

    public static void save(File config) {

        try {

            if (!KONAS_FOLDER.exists()) {
                KONAS_FOLDER.mkdir();
            }

            if (!config.exists()) {
                config.createNewFile();
            }

            JsonObject object = new JsonObject();

            object.add("Modules", getModuleArray());
            object.add("Friends", getFriendArray());
            object.add("Macros", getMacroArray());

            FileWriter writer = new FileWriter(config);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            gson.toJson(object, writer);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Cant write to config file!");
        }

        // saveAccounts();
    }

    private static JsonArray getFriendArray() {
        JsonArray friends = new JsonArray();
        for (Friend friend : Friends.friends) {
            friends.add(friend.getName());
        }
        return friends;
    }

    private static JsonObject getModuleArray() {
        JsonObject modulesObject = new JsonObject();
        for (Module m : ModuleManager.getModules()) {
            modulesObject.add(m.getName(), getModuleObject(m));
        }
        return modulesObject;
    }

    private static JsonArray getMacroArray() {
        JsonArray macroArray = new JsonArray();
        for (Macro macro : MacroManager.getMacros()) {
            JsonObject macroObj = new JsonObject();
            macroObj.addProperty("Name", macro.getName());
            macroObj.addProperty("Bind", macro.getBind());
            macroObj.addProperty("Text", macro.getText());
            macroArray.add(macroObj);
        }

        return macroArray;
    }

    public static JsonObject getModuleObject(Module m) {
        JsonObject attribs = new JsonObject();
        attribs.addProperty("Bind", m.getKeybind());
        attribs.addProperty("Enabled", m.isActive());
        attribs.addProperty("Visible", m.isVisible());
        attribs.addProperty("Hold", m.isHold());
        if (m.getSettingList() != null) {
            for (Setting s : m.getSettingList()) {
                if (s.getValue() instanceof Number) {
                    attribs.addProperty(s.getName(), (Number) s.getValue());
                } else if (s.getValue() instanceof Boolean) {
                    attribs.addProperty(s.getName(), (Boolean) s.getValue());
                } else if (s.getValue() instanceof Parent) {
                    attribs.addProperty(s.getName(), ((Parent) s.getValue()).isExtended());
                } else if (s.getValue() instanceof Enum || s.getValue() instanceof String) {
                    attribs.addProperty(s.getName(), String.valueOf(s.getValue()));
                } else if (s.getValue() instanceof ColorSetting) {

                    JsonArray array = new JsonArray();
                    array.add(((ColorSetting) s.getValue()).getRawColor());
                    array.add(((ColorSetting) s.getValue()).isCycle());

                    attribs.add(s.getName(), array);

                } else if (s.getValue() instanceof BlockListSetting) {
                    JsonArray array = new JsonArray();

                    for (String str : ((BlockListSetting) s.getValue()).getBlocksAsString()) {
                        array.add(str);
                    }

                    attribs.add(s.getName(), array);
                } else if (s.getValue() instanceof SubBind) {
                    attribs.addProperty(s.getName(), ((SubBind) s.getValue()).getKeyCode());
                }
            }
        }
        return attribs;
    }

    public static File getLastModified() {
        if (!KONAS_FOLDER.exists() || KONAS_FOLDER.listFiles() == null) {
            KONAS_FOLDER.mkdir();
            return CONFIG;
        }

        if (CONFIGS.listFiles() != null) {
            List<File> configFiles = Arrays.stream(CONFIGS.listFiles()).filter(f -> f.getName().endsWith(".json")).collect(Collectors.toList());
            configFiles.add(CONFIG);

            return configFiles.stream().max(Comparator.comparingLong(File::lastModified)).orElse(CONFIG);
        }

        return CONFIG;
    }
}
