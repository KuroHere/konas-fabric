package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.client.MouseButtonEvent;
import com.konasclient.konas.event.events.client.ScrollEvent;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method = "onMouseButton", at = @At("TAIL"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo info) {
        Konas.postEvent(MouseButtonEvent.get(button, action));
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    public void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        Konas.postEvent(ScrollEvent.get(vertical));
    }

}