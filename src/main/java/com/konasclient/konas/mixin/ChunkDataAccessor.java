package com.konasclient.konas.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ChunkBuilder.ChunkData.class)
public interface ChunkDataAccessor {
    @Accessor("initializedLayers")
    Set<RenderLayer> getInitializedLayers();

    @Accessor("empty")
    void setEmpty(boolean empty);

    @Accessor("nonEmptyLayers")
    Set<RenderLayer> getNonEmptyLayers();
}