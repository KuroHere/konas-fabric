package com.konasclient.konas.util.render.font;

import com.konasclient.konas.module.modules.client.ClickGUIModule;
import com.konasclient.konas.module.modules.client.FontModule;
import com.konasclient.konas.module.modules.render.Nametags;

public final class FontManager {
    public static boolean firstRun = true;

    public static void initFonts(String fontName) {
        ClickGUIModule.customFontRenderer = new CustomFontRenderer(fontName, FontModule.size.getValue(), FontModule.resolution.getValue());
        Nametags.customFontRenderer = new CustomFontRenderer(fontName, Nametags.fontSize.getValue(), 1);
        Nametags.enchantFontRenderer = new CustomFontRenderer(fontName, Nametags.enchantFontSize.getValue(), 1);
    }
}
