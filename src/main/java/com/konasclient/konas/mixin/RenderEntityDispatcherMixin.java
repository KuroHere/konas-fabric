package com.konasclient.konas.mixin;


import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.render.EntityRenderEvent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class RenderEntityDispatcherMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public <E extends Entity> void onRenderPre(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        EntityRenderEvent.Pre event = Konas.EVENT_BUS.post(EntityRenderEvent.Pre.get(entity, matrices, vertexConsumers));

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    public <E extends Entity> void onRenderPost(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        EntityRenderEvent.Post event = Konas.EVENT_BUS.post(EntityRenderEvent.Post.get(entity, matrices, vertexConsumers));
    }
}