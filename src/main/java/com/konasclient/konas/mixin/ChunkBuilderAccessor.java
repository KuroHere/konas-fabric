package com.konasclient.konas.mixin;

import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkBuilder.class)
public interface ChunkBuilderAccessor {
    @Accessor("buffers")
    BlockBufferBuilderStorage getBuffers();
}
