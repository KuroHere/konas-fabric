package com.konasclient.konas.util.render.font;

import com.konasclient.konas.module.modules.client.FontModule;
import com.konasclient.konas.util.render.Color;
import com.konasclient.konas.util.render.mesh.DrawMode;
import com.konasclient.konas.util.render.mesh.MeshBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormats;
import org.lwjgl.BufferUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class CustomFontRenderer implements IFontRenderer {
    private static final Color SHADOW_COLOR = new Color(60, 60, 60, 180);

    private final MeshBuilder mb = new MeshBuilder(16384);

    private final Font font;

    private double factor = 0D;

    public CustomFontRenderer(String name, int size, int resolution) {
        InputStream in = CustomFontRenderer.class.getResourceAsStream("/assets/konas/fonts/" + name + ".ttf");
        byte[] bytes = readBytes(in);
        ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length).put(bytes);

        int scaling = (int) Math.ceil(MinecraftClient.getInstance().getWindow().getScaleFactor()) + resolution;

        (buffer).flip();
        font = new Font(buffer, size * scaling);

        factor = 1D / scaling;

        mb.texture = true;
    }

    public static byte[] readBytes(InputStream in) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            byte[] buffer = new byte[256];
            int read;
            while ((read = in.read(buffer)) > 0) out.write(buffer, 0, read);

            in.close();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new byte[0];
    }

    @Override
    public int drawString(String text, float x, float y, int color) {
        return (int) render(text, x, y, new Color(color), false);
    }

    @Override
    public int drawStringWithShadow(String text, float x, float y, int color) {
        return (int) render(text, x, y, new Color(color), FontModule.shadows.getValue());
    }

    @Override
    public int drawCenteredString(String text, float x, float y, int color) {
        return (int) render(text, x - getStringWidth(text) / 2F, y, new Color(color), false);
    }

    @Override
    public float getStringWidth(String text) {
        return (float) font.getWidth(text, text.length()) * (float) factor;
    }

    @Override
    public int getFontHeight() {
        return (int) (font.getHeight() * factor);
    }

    @Override
    public float getStringHeight(String text) {
        return (float) font.getHeight() * (float) factor;
    }

    public double render(String text, double x, double y, Color color, boolean shadow) {
        mb.begin(null, DrawMode.Triangles, VertexFormats.POSITION_COLOR_TEXTURE);

        double r;
        if (shadow) {
            r = font.render(mb, text, x + 1, y + 1, SHADOW_COLOR, factor);
            font.render(mb, text, x, y, color, factor);
        } else r = font.render(mb, text, x, y, color, factor);

        font.texture.bindTexture();
        mb.end();
        return r;
    }
}
