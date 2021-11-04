package com.konasclient.konas.util.render.mesh;

import org.lwjgl.opengl.GL11;

public enum DrawMode {
    Triangles,
    Lines,
    LineLoop,
    LineStrip,
    Quads;

    public int toOpenGl() {
        if (this == Triangles) return GL11.GL_TRIANGLES;
        else if (this == Quads) return GL11.GL_QUADS;
        else if (this == LineLoop) return GL11.GL_LINE_LOOP;
        else if (this == LineStrip) return GL11.GL_LINE_STRIP;
        return GL11.GL_LINES;
    }
}