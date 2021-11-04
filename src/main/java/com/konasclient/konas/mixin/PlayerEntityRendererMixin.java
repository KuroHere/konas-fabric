package com.konasclient.konas.mixin;

import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.render.Chams;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    private VertexConsumerProvider lastVertexConsumers;
    private AbstractClientPlayerEntity lastPlayer;

    @Inject(method = "renderArm", at = @At("HEAD"))
    public void onRenderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        lastVertexConsumers = vertexConsumers;
        lastPlayer = player;
    }

    @Redirect(method = "renderArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"))
    private void onRenderArmModelPart(ModelPart modelPart, MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        Identifier texture = Chams.handTextures.getValue() || !ModuleManager.get(Chams.class).isActive() ? lastPlayer.getSkinTexture() : Chams.EMPTY_TEXTURE;
        if (ModuleManager.get(Chams.class).isActive()) {
            modelPart.render(matrices, lastVertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture)), light, OverlayTexture.DEFAULT_UV, Chams.hand.getValue().getRed() / 255F, Chams.hand.getValue().getGreen() / 255F, Chams.hand.getValue().getBlue() / 255F, Chams.hand.getValue().getAlpha() / 255F);
        } else {
            modelPart.render(matrices, lastVertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture)), light, OverlayTexture.DEFAULT_UV);
        }
    }
}
