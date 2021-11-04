package com.konasclient.konas.module.modules.client;

import com.konasclient.konas.event.events.render.RenderHudEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.ListenableSettingDecorator;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.render.font.DefaultFontRenderer;
import com.konasclient.konas.util.render.font.FontManager;
import com.konasclient.konas.util.render.font.FontRenderWrapper;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

public class FontModule extends Module {

    public static Setting<Boolean> shadows = new Setting<>("Shadows", true);

    public static ListenableSettingDecorator<Integer> size = new ListenableSettingDecorator<>("Size", 11, 20, 5, 1, (value) -> {
        if (!FontManager.firstRun) {
            FontManager.initFonts(FontModule.currentFont);
        }
    });
    public static ListenableSettingDecorator<Integer> resolution = new ListenableSettingDecorator<>("Resolution", 1, 5, 0, 1, (value) -> {
        if (!FontManager.firstRun) {
            FontManager.initFonts(FontModule.currentFont);
        }
    });

    public static String currentFont = "verdana";

    public FontModule() {
        super("font", "Customize your font", 0xFF42D9A4, Category.Client);
        toggle(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRenderHud(RenderHudEvent event) {
        if (FontManager.firstRun) {
            FontManager.firstRun = false;
            FontManager.initFonts(FontModule.currentFont);
        }
        if (FontRenderWrapper.getFontRenderer() != ClickGUIModule.customFontRenderer) {
            FontRenderWrapper.setFontRenderer(ClickGUIModule.customFontRenderer);
        }
    }

    public void onDisable() {
        if (FontRenderWrapper.getFontRenderer() != DefaultFontRenderer.INSTANCE) {
            FontRenderWrapper.setFontRenderer(DefaultFontRenderer.INSTANCE);
        }
    }
}
