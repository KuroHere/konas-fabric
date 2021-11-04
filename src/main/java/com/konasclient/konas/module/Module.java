package com.konasclient.konas.module;

import com.konasclient.konas.Konas;
import com.konasclient.konas.setting.Bind;
import com.konasclient.konas.setting.ListenableSettingDecorator;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.StringUtils;
import com.konasclient.konas.util.chat.Chat;
import net.minecraft.client.MinecraftClient;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

import static com.konasclient.konas.module.ModuleManager.MESSAGE_ID;

public abstract class Module {

    protected final MinecraftClient mc;

    private final String name;
    private final String title;
    private final String description;
    private final int color;
    private final Category category;
    private final ArrayList<String> aliases = new ArrayList<>();

    private final Setting<Bind> keybind = new Setting<>("Bind", new Bind(-1));
    private boolean hold = false;

    private boolean active;
    private boolean visible = true;

    public Module(String name, String description, int color, Category category, String... aliases) {
        this.name = name;
        this.title = StringUtils.nameToTitle(name);
        this.description = description;
        this.color = color;
        this.category = category;
        Collections.addAll(this.aliases, aliases);
        mc = MinecraftClient.getInstance();
    }

    public Module(String name, int color, Category category, String... aliases) {
        this(name, null, color, category, aliases);
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getMetadata() {
        return "";
    }

    public boolean hasDescription() {
        return description != null;
    }

    public ArrayList<String> getAliases() {
        return aliases;
    }

    public int getKeybind() {
        return keybind.getValue().getKeyCode();
    }

    public void setKeybind(int key) {
        this.keybind.setValue(new Bind(key));
    }

    public boolean isHold() {
        return hold;
    }

    public void setHold(boolean hold) {
        this.hold = hold;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void toggle() {
        toggle(!active);
    }

    public void toggle(boolean enable) {
        boolean wasActive = isActive();
        active = enable;
        if (shouldSendToggleMessage()) sendToggleMessage();

        if (active && !wasActive) {
            Konas.EVENT_BUS.subscribe(this);
            onEnable();
        } else if (!active && wasActive) {
            Konas.EVENT_BUS.unsubscribe(this);
            onDisable();
        }
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void sendToggleMessage(boolean toggled) {
        if (toggled) Chat.info(MESSAGE_ID, "%s (green)enabled", title);
        else Chat.info(MESSAGE_ID, "%s (red)disabled", title);
    }

    public void sendToggleMessage() {
        sendToggleMessage(active);
    }

    protected boolean shouldSendToggleMessage() {
        return true;
    }

    public Setting getSetting(String settingName) {
        for (Setting setting : getSettingList()) {
            if (setting.getName().equalsIgnoreCase(settingName)) return setting;
        }
        return null;
    }

    public ArrayList<Setting> getSettingList() {
        ArrayList<Setting> settingList = new ArrayList<>();

        for (Field field : this.getClass().getDeclaredFields()) {
            if (Setting.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    if (ListenableSettingDecorator.class.isAssignableFrom(field.getType())) {
                        settingList.add((ListenableSettingDecorator) field.get(this));
                    } else {
                        settingList.add((Setting) field.get(this));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        for (Field field : this.getClass().getSuperclass().getDeclaredFields()) {
            if (Setting.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);

                try {
                    settingList.add((Setting) field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return settingList;
    }

    public int getColor() {
        return color;
    }
}

