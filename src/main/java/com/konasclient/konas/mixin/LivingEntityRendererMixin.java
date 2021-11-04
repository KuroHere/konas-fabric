package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.render.EntityScaleEvent;
import com.konasclient.konas.event.events.render.RenderEntityEvent;
import com.konasclient.konas.event.events.render.RenderEntityModelEvent;
import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.render.Chams;
import com.konasclient.konas.module.modules.render.ESP;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    private T lastEntity;

    private float originalYaw;
    private float originalHeadYaw;
    private float originalBodyYaw;
    private float originalPitch;

    private float originalPrevYaw;
    private float originalPrevHeadYaw;
    private float originalPrevBodyYaw;
    private float originalPrevPitch;

    @Redirect(method = "getRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getTexture(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/Identifier;"))
    public Identifier onGetTexture(LivingEntityRenderer livingEntityRenderer, Entity entity) {
        if (ModuleManager.get(Chams.class).isActive() && !Chams.shouldRenderTexture(entity)) {
            return Chams.EMPTY_TEXTURE;
        }
        return livingEntityRenderer.getTexture(entity);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void onRenderPre(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && livingEntity == MinecraftClient.getInstance().player) {
            originalYaw = livingEntity.yaw;
            originalHeadYaw = livingEntity.headYaw;
            originalBodyYaw = livingEntity.bodyYaw;
            originalPitch = livingEntity.pitch;
            originalPrevYaw = livingEntity.prevYaw;
            originalPrevHeadYaw = livingEntity.prevHeadYaw;
            originalPrevBodyYaw = livingEntity.prevBodyYaw;
            originalPrevPitch = livingEntity.prevPitch;
            livingEntity.yaw = ((ClientPlayerEntityAccessor) MinecraftClient.getInstance().player).getLastYaw();
            livingEntity.headYaw = ((ClientPlayerEntityAccessor) MinecraftClient.getInstance().player).getLastYaw();
            livingEntity.bodyYaw = ((ClientPlayerEntityAccessor) MinecraftClient.getInstance().player).getLastYaw();
            livingEntity.pitch = ((ClientPlayerEntityAccessor) MinecraftClient.getInstance().player).getLastPitch();
            livingEntity.prevYaw = Konas.prevYaw;
            livingEntity.prevHeadYaw =  Konas.prevYaw;
            livingEntity.prevBodyYaw = Konas.prevYaw;
            livingEntity.prevPitch = Konas.prevPitch;
        }
        lastEntity = livingEntity;
        Konas.EVENT_BUS.post(RenderEntityEvent.Pre.get(lastEntity));
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRenderPost(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        Konas.EVENT_BUS.post(RenderEntityEvent.Post.get(lastEntity));
        if (MinecraftClient.getInstance().player != null && livingEntity == MinecraftClient.getInstance().player) {
            livingEntity.yaw = originalYaw;
            livingEntity.headYaw = originalHeadYaw;
            livingEntity.bodyYaw = originalBodyYaw;
            livingEntity.pitch = originalPitch;

            livingEntity.prevYaw = originalPrevYaw;
            livingEntity.prevHeadYaw = originalPrevHeadYaw;
            livingEntity.prevBodyYaw = originalPrevBodyYaw;
            livingEntity.prevPitch = originalPitch;
        }
    }

    private boolean head;
    private boolean helmet;
    private boolean jacket;
    private boolean leftArm;
    private boolean leftLeg;
    private boolean leftPantLeg;
    private boolean leftSleeve;
    private boolean rightArm;
    private boolean rightLeg;
    private boolean rightPantLeg;
    private boolean rightSleeve;
    private boolean torso;

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private void onRenderModel(EntityModel entityModel, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        RenderEntityModelEvent event = Konas.EVENT_BUS.post(RenderEntityModelEvent.get(lastEntity));
        if (!event.isCancelled()) {
            if (ESP.meshPass) {
                if (entityModel instanceof PlayerEntityModel) {
                    head = ((PlayerEntityModel) entityModel).head.visible;
                    helmet = ((PlayerEntityModel) entityModel).helmet.visible;
                    jacket = ((PlayerEntityModel) entityModel).jacket.visible;
                    leftArm = ((PlayerEntityModel) entityModel).leftArm.visible;
                    leftLeg = ((PlayerEntityModel) entityModel).leftLeg.visible;
                    leftPantLeg = ((PlayerEntityModel) entityModel).leftPantLeg.visible;
                    leftSleeve = ((PlayerEntityModel) entityModel).leftSleeve.visible;
                    rightArm = ((PlayerEntityModel) entityModel).rightArm.visible;
                    rightLeg = ((PlayerEntityModel) entityModel).rightLeg.visible;
                    rightPantLeg = ((PlayerEntityModel) entityModel).rightPantLeg.visible;
                    rightSleeve = ((PlayerEntityModel) entityModel).rightSleeve.visible;
                    torso = ((PlayerEntityModel) entityModel).torso.visible;
                    ((PlayerEntityModel) entityModel).head.visible = false;
                    ((PlayerEntityModel) entityModel).helmet.visible = true;
                    ((PlayerEntityModel) entityModel).jacket.visible = true;
                    ((PlayerEntityModel) entityModel).leftArm.visible = false;
                    ((PlayerEntityModel) entityModel).leftLeg.visible = false;
                    ((PlayerEntityModel) entityModel).leftPantLeg.visible = true;
                    ((PlayerEntityModel) entityModel).leftSleeve.visible = true;
                    ((PlayerEntityModel) entityModel).rightArm.visible = false;
                    ((PlayerEntityModel) entityModel).rightLeg.visible = false;
                    ((PlayerEntityModel) entityModel).rightPantLeg.visible = true;
                    ((PlayerEntityModel) entityModel).rightSleeve.visible = true;
                    ((PlayerEntityModel) entityModel).torso.visible = false;
                }
            }
            entityModel.render(matrices, vertices, light, overlay, event.red, event.green, event.blue, event.alpha);
            if (ESP.meshPass) {
                if (entityModel instanceof PlayerEntityModel) {
                    ((PlayerEntityModel) entityModel).head.visible = head;
                    ((PlayerEntityModel) entityModel).helmet.visible = helmet;
                    ((PlayerEntityModel) entityModel).jacket.visible = jacket;
                    ((PlayerEntityModel) entityModel).leftArm.visible = leftArm;
                    ((PlayerEntityModel) entityModel).leftLeg.visible = leftLeg;
                    ((PlayerEntityModel) entityModel).leftPantLeg.visible = leftPantLeg;
                    ((PlayerEntityModel) entityModel).leftSleeve.visible = leftSleeve;
                    ((PlayerEntityModel) entityModel).rightArm.visible = rightArm;
                    ((PlayerEntityModel) entityModel).rightLeg.visible = rightLeg;
                    ((PlayerEntityModel) entityModel).rightPantLeg.visible = rightPantLeg;
                    ((PlayerEntityModel) entityModel).rightSleeve.visible = rightSleeve;
                    ((PlayerEntityModel) entityModel).torso.visible = torso;
                }
            }
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/FeatureRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/Entity;FFFFFF)V"))
    public void onRenderFeature(FeatureRenderer featureRenderer, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Entity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (ESP.meshPass) return;
        featureRenderer.render(matrices, vertexConsumers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V", ordinal = 0))
    private void onScale(Args args) {
        EntityScaleEvent event = Konas.EVENT_BUS.post(EntityScaleEvent.get(lastEntity, (float) args.get(0), (float) args.get(1), (float) args.get(2)));

        args.set(0, event.x);
        args.set(1, event.y);
        args.set(2, event.z);
    }
}
