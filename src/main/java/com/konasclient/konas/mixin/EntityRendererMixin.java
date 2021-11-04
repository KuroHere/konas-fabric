package com.konasclient.konas.mixin;

import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.render.ESP;
import com.konasclient.konas.module.modules.render.Nametags;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void onHasLabel(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (ESP.meshPass) ci.cancel();
        if (entity instanceof PlayerEntity && ModuleManager.get(Nametags.class).isActive()) ci.cancel();
    }
}
