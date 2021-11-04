package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.client.KeyEvent;
import net.minecraft.client.Keyboard;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void onKey(long window, int key, int scancode, int i, int j, CallbackInfo ci) {
        if (key != GLFW.GLFW_KEY_UNKNOWN) {
            KeyEvent event = Konas.EVENT_BUS.post(KeyEvent.get(key, i));

            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }
}
