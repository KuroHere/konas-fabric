package com.konasclient.konas.event.events.shader;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.entity.Entity;

public class ShaderEntityRenderEvent {
    private static ShaderEntityRenderEvent INSTANCE = new ShaderEntityRenderEvent();

    public Entity entity;

    public Framebuffer fb;
    public OutlineVertexConsumerProvider outlineVertexConsumers;
    public boolean doublePass;

    public static ShaderEntityRenderEvent get(Entity entity) {
        INSTANCE.entity = entity;
        INSTANCE.fb = null;
        INSTANCE.outlineVertexConsumers = null;
        INSTANCE.doublePass = false;
        return INSTANCE;
    }
}
