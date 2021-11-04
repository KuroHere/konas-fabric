package com.konasclient.konas.event.events.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

public class RenderEvent {
    private static final RenderEvent INSTANCE = new RenderEvent();

    public MatrixStack matrices;
    public Matrix4f model;
    public Matrix4f projection;
    public float tickDelta;
    public double offsetX, offsetY, offsetZ;

    public static RenderEvent get(MatrixStack matrices, Matrix4f model, Matrix4f projection, float tickDelta, double offsetX, double offsetY, double offsetZ) {
        INSTANCE.matrices = matrices;
        INSTANCE.model = model;
        INSTANCE.projection = projection;
        INSTANCE.tickDelta = tickDelta;
        INSTANCE.offsetX = offsetX;
        INSTANCE.offsetY = offsetY;
        INSTANCE.offsetZ = offsetZ;
        return INSTANCE;
    }
}
