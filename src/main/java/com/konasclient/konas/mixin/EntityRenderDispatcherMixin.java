package com.konasclient.konas.mixin;

import com.konasclient.konas.module.modules.render.ESP;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "renderShadow", at = @At("HEAD"), cancellable = true)
    private static void onRenderShadow(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity, float opacity, float tickDelta, WorldView world, float radius, CallbackInfo ci) {
        if (ESP.meshPass) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
    private void onRenderFire(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity, CallbackInfo ci) {
        if (ESP.meshPass) {
            ci.cancel();
        }
    }

    @Inject(method = "renderHitbox", at = @At("HEAD"), cancellable = true)
    private void onRenderHitbox(MatrixStack matrices, VertexConsumer vertices, Entity entity, float tickDelta, CallbackInfo ci) {
        if (ESP.meshPass) {
            ci.cancel();
        }
    }
}
