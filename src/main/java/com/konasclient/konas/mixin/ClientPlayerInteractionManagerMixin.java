package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.player.BlockReachDistanceEvent;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Inject(method = "getReachDistance", at = @At("RETURN"), cancellable = true)
    private void onGetReachDistance(CallbackInfoReturnable<Float> cir) {
        BlockReachDistanceEvent event = Konas.postEvent(BlockReachDistanceEvent.get(cir.getReturnValue()));
        Konas.postEvent(event);
        cir.setReturnValue(event.reachDistance);
    }

}
