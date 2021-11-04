package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.player.PushOutOfBlocksEvent;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;

@Mixin(FlowableFluid.class)
public class FlowableFluidMixin {

    @Redirect(method = "getVelocity", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", ordinal = 0))
    public boolean onGetVelocity(Iterator<Direction> var9) {
        PushOutOfBlocksEvent event = Konas.postEvent(PushOutOfBlocksEvent.get());
        if (event.isCancelled()) {
            return false;
        }

        return var9.hasNext();
    }

}
