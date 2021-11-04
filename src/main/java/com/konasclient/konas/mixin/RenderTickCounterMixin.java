package com.konasclient.konas.mixin;

import com.konasclient.konas.util.client.TimerManager;
import net.minecraft.client.render.RenderTickCounter;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.class)
public class RenderTickCounterMixin {
    @Shadow
    public float lastFrameDuration;

    @Inject(method = "beginRenderTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter;prevTimeMillis:J", opcode = Opcodes.PUTFIELD))
    private void onBeingRenderTick(long timeMillis, CallbackInfoReturnable<Integer> info) {
        lastFrameDuration *= TimerManager.getTimerSpeed();
    }
}
