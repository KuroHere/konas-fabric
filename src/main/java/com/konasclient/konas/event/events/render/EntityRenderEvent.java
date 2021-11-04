package com.konasclient.konas.event.events.render;

import com.konasclient.konas.event.Cancellable;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class EntityRenderEvent extends Cancellable {
    public Entity entity;
    public MatrixStack matrix;
    public VertexConsumerProvider vertex;

    public static class Pre extends EntityRenderEvent {
        private static final Pre INSTANCE = new Pre();

        public static Pre get(Entity entity, MatrixStack matrix, VertexConsumerProvider vertex) {
            INSTANCE.entity = entity;
            INSTANCE.matrix = matrix;
            INSTANCE.vertex = vertex;
            INSTANCE.setCancelled(false);
            return INSTANCE;
        }
    }

    public static class Post extends EntityRenderEvent {
        private static final Post INSTANCE = new Post();

        public static Post get(Entity entity, MatrixStack matrix, VertexConsumerProvider vertex) {
            INSTANCE.entity = entity;
            INSTANCE.matrix = matrix;
            INSTANCE.vertex = vertex;
            INSTANCE.setCancelled(false);
            return INSTANCE;
        }
    }
}
