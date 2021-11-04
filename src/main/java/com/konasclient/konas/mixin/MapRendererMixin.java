package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.render.DrawMapEvent;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapRenderer.class)
public class MapRendererMixin {

    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    public void draw(MatrixStack matrices, VertexConsumerProvider vertexConsumers, MapState mapState, boolean bl, int light, CallbackInfo ci) {
        DrawMapEvent event = Konas.EVENT_BUS.post(DrawMapEvent.get());

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}