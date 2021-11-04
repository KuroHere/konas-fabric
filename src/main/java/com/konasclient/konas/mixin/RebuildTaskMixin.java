package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.shader.ShaderBlockRenderEvent;
import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.render.WorldOutline;
import com.konasclient.konas.util.render.KonasRenderLayers;
import com.konasclient.konas.util.render.WorldOutlineRenderLayers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

@Mixin(targets = "net.minecraft.client.render.chunk.ChunkBuilder$BuiltChunk$RebuildTask")
public class RebuildTaskMixin {

    @Inject(method = "Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk$RebuildTask;render(FFFLnet/minecraft/client/render/chunk/ChunkBuilder$ChunkData;Lnet/minecraft/client/render/chunk/BlockBufferBuilderStorage;)Ljava/util/Set;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onBlockRender(float f, float g, float h, ChunkBuilder.ChunkData chunkData, BlockBufferBuilderStorage blockBufferBuilderStorage, CallbackInfoReturnable<Set<BlockEntity>> cir, int i, BlockPos blockPos, BlockPos blockPos2, ChunkOcclusionDataBuilder chunkOcclusionDataBuilder, Set set, ChunkRendererRegion chunkRendererRegion, MatrixStack matrixStack, Random random, BlockRenderManager blockRenderManager, Iterator var15, BlockPos blockPos3, BlockState blockState, RenderLayer renderLayer2, BufferBuilder bufferBuilder2) {
        ShaderBlockRenderEvent event = Konas.EVENT_BUS.post(ShaderBlockRenderEvent.get(blockPos3, blockState));
        if (event.isCancelled()) {
            RenderLayer layer = KonasRenderLayers.INSTANCE.getSolidFiltered();
            BufferBuilder builder = blockBufferBuilderStorage.get(layer);
            ChunkDataAccessor iChunkData = (ChunkDataAccessor) chunkData;
            if (iChunkData.getInitializedLayers().add(layer)) {
                builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
            }

            if (blockRenderManager.renderBlock(blockState, blockPos3, chunkRendererRegion, matrixStack, builder, false, random)) {
                iChunkData.setEmpty(false);
                iChunkData.getNonEmptyLayers().add(layer);
            }
        }

        try {
            if (ModuleManager.get(WorldOutline.class) != null && ModuleManager.get(WorldOutline.class).isActive()) {
                if (WorldOutline.check(event.state)) {
                    RenderLayer layer = WorldOutlineRenderLayers.INSTANCE.getSolidFiltered();
                    BufferBuilder builder = blockBufferBuilderStorage.get(layer);
                    ChunkDataAccessor iChunkData = (ChunkDataAccessor) chunkData;
                    if (iChunkData.getInitializedLayers().add(layer)) {
                        builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
                    }

                    if (blockRenderManager.renderBlock(event.state, blockPos3, chunkRendererRegion, matrixStack, builder, false, random)) {
                        iChunkData.setEmpty(false);
                        iChunkData.getNonEmptyLayers().add(layer);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

}