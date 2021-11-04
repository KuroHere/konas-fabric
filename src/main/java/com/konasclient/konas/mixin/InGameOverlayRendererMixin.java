package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.render.RenderOverlayEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {

    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void onRenderFireOverlay(MinecraftClient minecraftClient, MatrixStack matrixStack, CallbackInfo ci) {
        RenderOverlayEvent event = Konas.EVENT_BUS.post(RenderOverlayEvent.get(RenderOverlayEvent.Type.Fire));
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderUnderwaterOverlay", at = @At("HEAD"), cancellable = true)
    private static void onRenderUnderwaterOverlay(MinecraftClient minecraftClient, MatrixStack matrixStack, CallbackInfo ci) {
        RenderOverlayEvent event = Konas.EVENT_BUS.post(RenderOverlayEvent.get(RenderOverlayEvent.Type.Liquid));
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}