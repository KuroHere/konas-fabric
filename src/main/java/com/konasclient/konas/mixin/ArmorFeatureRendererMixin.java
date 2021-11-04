package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.render.RenderArmorEvent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> {

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private void onRenderArmor(MatrixStack matrices, VertexConsumerProvider vertexConsumers, T livingEntity, EquipmentSlot equipmentSlot, int i, A bipedEntityModel, CallbackInfo ci) {
        RenderArmorEvent event = Konas.EVENT_BUS.post(RenderArmorEvent.get(equipmentSlot));

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
