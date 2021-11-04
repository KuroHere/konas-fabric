package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.world.RecalculateSkylightEvent;
import net.minecraft.world.chunk.light.ChunkSkyLightProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSkyLightProvider.class)
public class ChunkSkylightProviderMixin {

    @Inject(method = "recalculateLevel", at = @At("HEAD"), cancellable = true)
    protected void recalculateLevel(long id, long excludedId, int maxLevel, CallbackInfoReturnable<Integer> cir) {
        if (Konas.EVENT_BUS.post(RecalculateSkylightEvent.get()).isCancelled()) {
            cir.setReturnValue(15);
        }
    }
}