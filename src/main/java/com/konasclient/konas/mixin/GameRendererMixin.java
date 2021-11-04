package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.render.*;
import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.render.FullBright;
import com.konasclient.konas.util.render.Matrices;
import com.konasclient.konas.util.render.rendering.ModelRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow @Final private MinecraftClient client;

    private boolean a = false;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRenderHead(float tickDelta, long startTime, boolean tick, CallbackInfo info) {
        a = false;
    }

    private double startingGamma = -1D;

    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void onRenderWorldHead(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo info) {
        Matrices.begin(matrix);
        Matrices.push();
        RenderSystem.pushMatrix();

        a = true;

        if (ModuleManager.get(FullBright.class).isActive() && FullBright.gamma.getValue()) {
            startingGamma = MinecraftClient.getInstance().options.gamma;
            MinecraftClient.getInstance().options.gamma = 69420;
        }
    }

    @Inject(method = "renderWorld", at = @At("TAIL"))
    private void onRenderWorldHeadTail(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo info) {
        if (startingGamma != -1D) {
            MinecraftClient.getInstance().options.gamma = startingGamma;
            startingGamma = -1D;
        }
    }

    @Inject(method = "renderWorld", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = {"ldc=hand"}), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onRenderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo info, boolean bl, Camera camera, MatrixStack matrixStack2, Matrix4f matrix4f) {
        if (MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().world == null) return;

        client.getProfiler().push("konas-client_render");

        RenderEvent event = RenderEvent.get(matrix, matrix.peek().getModel().copy(), matrix4f, tickDelta, camera.getPos().x, camera.getPos().y, camera.getPos().z);

        ModelRenderer.begin(event);
        Konas.postEvent(event);
        ModelRenderer.end();

        client.getProfiler().pop();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V", ordinal = 0))
    private void onRenderBeforeGuiRender(float tickDelta, long startTime, boolean tick, CallbackInfo info) {
        if (a) {
            Matrices.pop();
            RenderSystem.popMatrix();
        }
    }

    @Inject(method = "bobViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void onBobViewWhenHurt(MatrixStack matrixStack, float f, CallbackInfo ci) {
        HurtBobEvent event = Konas.EVENT_BUS.post(HurtBobEvent.get());

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void onShowFloatingItem(ItemStack floatingItem, CallbackInfo ci) {
        ShowFloatingItemEvent event = Konas.EVENT_BUS.post(ShowFloatingItemEvent.get(floatingItem));

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F", ordinal = 0), require = 0)
    private float onLerp(float delta, float first, float second) {
        if (Konas.EVENT_BUS.post(RenderNauseaWobbleEvent.get()).isCancelled()) {
            return 0;
        }

        return MathHelper.lerp(delta, first, second);
    }
}
