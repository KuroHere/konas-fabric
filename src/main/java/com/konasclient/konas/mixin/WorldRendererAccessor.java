package com.konasclient.konas.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {
    @Accessor("blockBreakingInfos")
    Int2ObjectMap<BlockBreakingInfo> getBlockBreakingInfos();

    @Accessor("entityOutlinesFramebuffer")
    void setEntityOutlinesFramebuffer(Framebuffer framebuffer);

    @Accessor("capturedFrustum")
    Frustum getCapturedFrustum();

    @Accessor("chunkBuilder")
    ChunkBuilder getChunkBuilder();
}
