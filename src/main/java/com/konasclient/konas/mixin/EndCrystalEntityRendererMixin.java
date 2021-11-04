package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.render.EntityScaleEvent;
import com.konasclient.konas.event.events.render.RenderEntityEvent;
import com.konasclient.konas.event.events.render.RenderEntityModelEvent;
import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.render.Chams;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(EndCrystalEntityRenderer.class)
public class EndCrystalEntityRendererMixin {
    @Shadow @Final private static Identifier TEXTURE;
    @Mutable
    @Shadow @Final private static RenderLayer END_CRYSTAL;
    private EndCrystalEntity lastEntity;

    @Inject(method = "render", at = @At("HEAD"))
    public void onRender(EndCrystalEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        lastEntity = livingEntity;
        END_CRYSTAL = RenderLayer.getEntityTranslucent(!ModuleManager.get(Chams.class).isActive() || Chams.shouldRenderTexture(livingEntity) ? TEXTURE : Chams.EMPTY_TEXTURE);
        Konas.EVENT_BUS.post(RenderEntityEvent.Pre.get(lastEntity));
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRenderPost(EndCrystalEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        Konas.EVENT_BUS.post(RenderEntityEvent.Post.get(lastEntity));
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"))
    public void onRenderPart(ModelPart modelPart, MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        RenderEntityModelEvent event = Konas.EVENT_BUS.post(RenderEntityModelEvent.get(lastEntity));
        if (!event.isCancelled()) {
            modelPart.render(matrices, vertices, light, overlay, event.red, event.green, event.blue, event.alpha);
        }
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V", ordinal = 0))
    private void onScale(Args args) {
        EntityScaleEvent event = Konas.EVENT_BUS.post(EntityScaleEvent.get(lastEntity, (float) args.get(0), (float) args.get(1), (float) args.get(2)));

        args.set(0, event.x);
        args.set(1, event.y);
        args.set(2, event.z);
    }

}
