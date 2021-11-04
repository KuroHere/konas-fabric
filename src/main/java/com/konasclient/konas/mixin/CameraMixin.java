package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.render.CameraClipEvent;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    private void onClipToSpace(double desiredCameraDistance, CallbackInfoReturnable<Double> cir) {
        CameraClipEvent event = Konas.EVENT_BUS.post(CameraClipEvent.get(desiredCameraDistance));
        if (event.isCancelled()) {
            cir.setReturnValue(event.distance);
        }
    }
}
