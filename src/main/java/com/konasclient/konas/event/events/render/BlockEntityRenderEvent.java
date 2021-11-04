package com.konasclient.konas.event.events.render;

import com.konasclient.konas.event.Cancellable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class BlockEntityRenderEvent extends Cancellable {
    public BlockEntity blockEntity;
    public MatrixStack matrix;
    public VertexConsumerProvider vertex;

    public static class Pre extends BlockEntityRenderEvent {
        private static final Pre INSTANCE = new Pre();

        public static Pre get(BlockEntity blockEntity, MatrixStack matrix, VertexConsumerProvider vertex) {
            INSTANCE.blockEntity = blockEntity;
            INSTANCE.matrix = matrix;
            INSTANCE.vertex = vertex;
            INSTANCE.setCancelled(false);
            return INSTANCE;
        }
    }

    public static class Post extends BlockEntityRenderEvent {
        private static final Post INSTANCE = new Post();

        public static Post get(BlockEntity blockEntity, MatrixStack matrix, VertexConsumerProvider vertex) {
            INSTANCE.blockEntity = blockEntity;
            INSTANCE.matrix = matrix;
            INSTANCE.vertex = vertex;
            INSTANCE.setCancelled(false);
            return INSTANCE;
        }
    }
}
