package com.konasclient.konas.util.render.gui;

import com.konasclient.konas.util.render.Color;
import com.konasclient.konas.util.render.mesh.DrawMode;
import com.konasclient.konas.util.render.mesh.MeshBuilder;
import net.minecraft.client.render.VertexFormats;

public class GuiRenderWrapper {
    private static final MeshBuilder mb = new MeshBuilder();

    public static void drawRect(float x, float y, float w, float h, int color) {
        mb.begin(null, DrawMode.Triangles, VertexFormats.POSITION_COLOR);
        mb.quad(x, y, w, h, new Color(color));
        mb.end();
    }

    public static void draw1DGradientRect(float x, float y, float width, float height, int startColor, int endColor) {
        mb.begin(null, DrawMode.Triangles, VertexFormats.POSITION_COLOR);
        mb.gradientQuad(x, y, width, height, new Color(startColor), new Color(endColor));
        mb.end();
    }

    public static void draw2DGradientRect(float x, float y, float width, float height, int leftBottomColor, int leftTopColor, int rightBottomColor, int rightTopColor) {
        mb.begin(null, DrawMode.Triangles, VertexFormats.POSITION_COLOR);
        mb.quad(x, y, width, height, new Color(leftTopColor), new Color(rightTopColor), new Color(rightBottomColor), new Color(leftBottomColor));
        mb.end();
    }

    public static void drawOutlineRect(float x, float y, float width, float height, int color) {
        mb.begin(null, DrawMode.Triangles, VertexFormats.POSITION_COLOR);
        mb.boxEdges(x, y, width, height, new Color(color));
        mb.end();
    }
}
