package com.konasclient.konas.mixin;

import com.konasclient.konas.util.render.KonasRenderLayers;
import com.konasclient.konas.util.render.WorldOutlineRenderLayers;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.stream.Collectors;

@Mixin(BlockBufferBuilderStorage.class)
public class BlockBufferBuilderStorageMixin {
    @Shadow
    @Final
    private Map<RenderLayer, BufferBuilder> builders;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(CallbackInfo ci) {
        builders.putAll(KonasRenderLayers.INSTANCE.getLayers().stream().collect(Collectors.toMap(renderLayer -> renderLayer, renderLayer -> new BufferBuilder(renderLayer.getExpectedBufferSize()))));
        builders.putAll(WorldOutlineRenderLayers.INSTANCE.getLayers().stream().collect(Collectors.toMap(renderLayer -> renderLayer, renderLayer -> new BufferBuilder(renderLayer.getExpectedBufferSize()))));
    }
}
