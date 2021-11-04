package com.konasclient.konas.util.render.font;

import com.konasclient.konas.Konas;
import net.minecraft.client.util.math.MatrixStack;

public class DefaultFontRenderer implements IFontRenderer {

    public static DefaultFontRenderer INSTANCE = new DefaultFontRenderer();

    private DefaultFontRenderer() {
    }

    @Override
    public int drawString(String text, float x, float y, int color) {
        return Konas.mc.textRenderer.draw(new MatrixStack(), text, x, y, color);
    }

    @Override
    public int drawStringWithShadow(String text, float x, float y, int color) {
        return Konas.mc.textRenderer.drawWithShadow(new MatrixStack(), text, x, y, color);
    }

    @Override
    public int drawCenteredString(String text, float x, float y, int color) {
        return Konas.mc.textRenderer.draw(new MatrixStack(), text, x - Konas.mc.textRenderer.getWidth(text) / 2F, y, color);
    }

    @Override
    public float getStringWidth(String text) {
        return Konas.mc.textRenderer.getWidth(text);
    }

    @Override
    public int getFontHeight() {
        return Konas.mc.textRenderer.fontHeight;
    }

    @Override
    public float getStringHeight(String text) {
        return Konas.mc.textRenderer.fontHeight - 0.66F;
    }


}
