package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.player.ClipAtLedgeEvent;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
    protected void clipAtLedge(CallbackInfoReturnable<Boolean> info) {
        ClipAtLedgeEvent event = Konas.EVENT_BUS.post(ClipAtLedgeEvent.get());

        if (event.isCancelled()) {
            info.setReturnValue(true);
        }
    }
}
