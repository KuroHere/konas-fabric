package com.konasclient.konas.util.render.mesh;

import com.konasclient.konas.event.events.render.RenderEvent;
import com.konasclient.konas.util.render.Color;
import com.konasclient.konas.util.render.Matrices;
import com.konasclient.konas.util.render.geometry.BlockGeometryMasks;
import com.konasclient.konas.util.render.texture.TextureRegion;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormat;

import static org.lwjgl.opengl.GL11.*;

public class MeshBuilder {
    public BufferBuilder buffer;
    public double alpha = 1;
    public boolean depthTest = false;
    public boolean texture = false;
    public float lineWidth = 1F;
    private double offsetX, offsetY, offsetZ;
    public boolean noOffset = false;
    private int count;

    public MeshBuilder(int initialCapacity) {
        buffer = new BufferBuilder(initialCapacity);
    }

    public MeshBuilder() {
        buffer = new BufferBuilder(2097152);
    }

    public void begin(RenderEvent event, DrawMode drawMode, VertexFormat format) {
        if (event != null) {
            offsetX = -event.offsetX;
            offsetY = -event.offsetY;
            offsetZ = -event.offsetZ;
        } else {
            offsetX = 0;
            offsetY = 0;
            offsetZ = 0;
        }

        buffer.begin(drawMode.toOpenGl(), format);
        count = 0;
    }

    public void end() {
        buffer.end();

        prepare();

        BufferRenderer.draw(buffer);

        release();
    }

    public void prepare() {
        glPushMatrix();
        if (!noOffset) {
            RenderSystem.multMatrix(Matrices.getTop());
        }

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        if (depthTest) RenderSystem.enableDepthTest();
        else RenderSystem.disableDepthTest();
        RenderSystem.disableAlphaTest();
        if (texture) RenderSystem.enableTexture();
        else RenderSystem.disableTexture();
        RenderSystem.disableLighting();
        RenderSystem.disableCull();
        glEnable(GL_LINE_SMOOTH);
        RenderSystem.lineWidth(lineWidth);
        RenderSystem.color4f(1, 1, 1, 1);
        GlStateManager.shadeModel(GL_SMOOTH);
    }

    public void release() {
        RenderSystem.enableAlphaTest();
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        glDisable(GL_LINE_SMOOTH);

        glPopMatrix();
    }

    public boolean isBuilding() {
        return buffer.isBuilding();
    }

    public MeshBuilder pos(double x, double y, double z) {
        if (noOffset) {
            buffer.vertex(x, y, z);
        } else {
            buffer.vertex(x + offsetX, y + offsetY, z + offsetZ);
        }
        return this;
    }

    public MeshBuilder texture(double x, double y) {
        buffer.texture((float) x, (float) y);
        return this;
    }

    public MeshBuilder color(Color color) {
        buffer.color(color.r / 255f, color.g / 255f, color.b / 255f, color.a / 255f * (float) alpha);
        return this;
    }

    public MeshBuilder color(int color) {
        buffer.color(Color.toRGBAR(color) / 255f, Color.toRGBAG(color) / 255f, Color.toRGBAB(color) / 255f, Color.toRGBAA(color) / 255f * (float) alpha);
        return this;
    }

    public void endVertex() {
        buffer.next();
        count++;
    }

    // NORMAL

    public void quad(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, Color color) {
        pos(x1, y1, z1).color(color).endVertex();
        pos(x2, y2, z2).color(color).endVertex();
        pos(x3, y3, z3).color(color).endVertex();

        pos(x1, y1, z1).color(color).endVertex();
        pos(x3, y3, z3).color(color).endVertex();
        pos(x4, y4, z4).color(color).endVertex();
    }

    public void quad(double x, double y, double width, double height, Color color) {
        quad(x, y, 0, x + width, y, 0, x + width, y + height, 0, x, y + height, 0, color);
    }

    public void horizontalQuad(double x1, double z1, double x2, double z2, double y, Color color) {
        quad(x1, y, z1, x1, y, z2, x2, y, z2, x2, y, z1, color);
    }

    public void verticalQuad(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
        quad(x1, y1, z1, x1, y2, z1, x2, y2, z2, x2, y1, z2, color);
    }

    public void gradientQuad(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, Color startColor, Color endColor) {
        pos(x1, y1, z1).color(startColor).endVertex();
        pos(x2, y2, z2).color(endColor).endVertex();
        pos(x3, y3, z3).color(endColor).endVertex();

        pos(x1, y1, z1).color(startColor).endVertex();
        pos(x3, y3, z3).color(endColor).endVertex();
        pos(x4, y4, z4).color(startColor).endVertex();
    }

    public void gradientQuad(double x, double y, double width, double height, Color startColor, Color endColor) {
        gradientQuad(x, y, 0, x + width, y, 0, x + width, y + height, 0, x, y + height, 0, startColor, endColor);
    }

    public void quad(double x, double y, double width, double height, Color cTopLeft, Color cTopRight, Color cBottomRight, Color cBottomLeft) {
        pos(x, y, 0).color(cTopLeft).endVertex();
        pos(x + width, y, 0).color(cTopRight).endVertex();
        pos(x + width, y + height, 0).color(cBottomRight).endVertex();

        pos(x, y, 0).color(cTopLeft).endVertex();
        pos(x + width, y + height, 0).color(cBottomRight).endVertex();
        pos(x, y + height, 0).color(cBottomLeft).endVertex();
    }

    public void texQuad(double x, double y, double width, double height, TextureRegion tex, Color color) {
        pos(x, y, 0).color(color).texture(tex.x1, tex.y1).endVertex();
        pos(x + width, y, 0).color(color).texture(tex.x2, tex.y1).endVertex();
        pos(x + width, y + height, 0).color(color).texture(tex.x2, tex.y2).endVertex();

        pos(x, y, 0).color(color).texture(tex.x1, tex.y1).endVertex();
        pos(x + width, y + height, 0).color(color).texture(tex.x2, tex.y2).endVertex();
        pos(x, y + height, 0).color(color).texture(tex.x1, tex.y2).endVertex();
    }

    public void boxSides(double x1, double y1, double z1, double x2, double y2, double z2, Color color, int excludeDir) {
        if (BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.DOWN))
            quad(x1, y1, z1, x1, y1, z2, x2, y1, z2, x2, y1, z1, color); // Bottom
        if (BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.UP))
            quad(x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, color); // Top

        if (BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.NORTH))
            quad(x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, color); // Front
        if (BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.SOUTH))
            quad(x1, y1, z2, x1, y2, z2, x2, y2, z2, x2, y1, z2, color); // Back

        if (BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.WEST))
            quad(x1, y1, z1, x1, y2, z1, x1, y2, z2, x1, y1, z2, color); // Left
        if (BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.EAST))
            quad(x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, color); // Right
    }

    public void gradientBoxSides(double x1, double y, double z1, double x2, double z2, double height, Color startColor, Color endColor, boolean reverse) {
        gradientQuad(x1, y, z1, x1, y + height, z1, x2, y + height, z1, x2, y, z1, reverse ? endColor : startColor, reverse ? startColor : endColor);
        gradientQuad(x1, y, z2, x1, y + height, z2, x2, y + height, z2, x2, y, z2, reverse ? endColor : startColor, reverse ? startColor : endColor);
        gradientQuad(x1, y, z1, x1, y + height, z1, x1, y + height, z2, x1, y, z2, reverse ? endColor : startColor, reverse ? startColor : endColor);
        gradientQuad(x2, y, z1, x2, y + height, z1, x2, y + height, z2, x2, y, z2, reverse ? endColor : startColor, reverse ? startColor : endColor);
    }

    // LINES

    public void line(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
        pos(x1, y1, z1).color(color).endVertex();
        pos(x2, y2, z2).color(color).endVertex();
    }

    public void gradientLine(double x1, double y1, double z1, double x2, double y2, double z2, Color startColor, Color endColor) {
        pos(x1, y1, z1).color(startColor).endVertex();
        pos(x2, y2, z2).color(endColor).endVertex();
    }

    public void boxEdges(double x1, double y1, double z1, double x2, double y2, double z2, Color color, int excludeDir) {
        if (BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.WEST) && BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.NORTH))
            line(x1, y1, z1, x1, y2, z1, color);
        if (BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.WEST) && BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.SOUTH))
            line(x1, y1, z2, x1, y2, z2, color);
        if (BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.EAST) && BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.NORTH))
            line(x2, y1, z1, x2, y2, z1, color);
        if (BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.EAST) && BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.SOUTH))
            line(x2, y1, z2, x2, y2, z2, color);

        if (BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.NORTH)) line(x1, y1, z1, x2, y1, z1, color);
        if (BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.NORTH)) line(x1, y2, z1, x2, y2, z1, color);
        if (BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.SOUTH)) line(x1, y1, z2, x2, y1, z2, color);
        if (BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.SOUTH)) line(x1, y2, z2, x2, y2, z2, color);

        if (BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.WEST)) line(x1, y1, z1, x1, y1, z2, color);
        if (BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.WEST)) line(x1, y2, z1, x1, y2, z2, color);
        if (BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.EAST)) line(x2, y1, z1, x2, y1, z2, color);
        if (BlockGeometryMasks.is(excludeDir, BlockGeometryMasks.EAST)) line(x2, y2, z1, x2, y2, z2, color);
    }

    public void boxEdges(double x, double y, double width, double height, Color color) {
        boxEdges(x, y, 0, x + width, y + height, 0, color, 0);
    }

    public void gradientVerticalBox(double x, double y, double z, double x2, double z2, double height, Color startColor, Color endColor, boolean reverse) {
        gradientLine(x, y, z, x, y + height, z, reverse ? endColor : startColor, reverse ? startColor : endColor);
        gradientLine(x2, y, z, x2, y + height, z, reverse ? endColor : startColor, reverse ? startColor : endColor);
        gradientLine(x, y, z2, x, y + height, z2, reverse ? endColor : startColor, reverse ? startColor : endColor);
        gradientLine(x2, y, z2, x2, y + height, z2, reverse ? endColor : startColor, reverse ? startColor : endColor);
    }
}
