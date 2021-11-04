package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.client.LoadResourcePackEvent;
import com.konasclient.konas.event.events.client.ResizeEvent;
import com.konasclient.konas.event.events.render.*;
import com.konasclient.konas.event.events.shader.ShaderEntityRenderEvent;
import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.client.FontModule;
import com.konasclient.konas.module.modules.render.*;
import com.konasclient.konas.util.render.KonasRenderLayers;
import com.konasclient.konas.util.render.WorldOutlineRenderLayers;
import com.konasclient.konas.util.render.font.FontManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow @Nullable private Framebuffer entityOutlinesFramebuffer;

    @Shadow protected abstract void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers);

    @Shadow protected abstract void renderLayer(RenderLayer renderLayer, MatrixStack matrixStack, double d, double e, double f);

    @Inject(method = "checkEmpty", at = @At("HEAD"), cancellable = true)
    private void onCheckEmpty(MatrixStack matrixStack, CallbackInfo info) {
        info.cancel();
    }

    @Inject(method = "renderWorldBorder", at = @At("HEAD"), cancellable = true)
    public void onRenderWorldBorder(Camera camera, CallbackInfo ci) {
        if (ModuleManager.get(NoRender.class).isActive() && NoRender.worldBorder.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        Matrix4f matrix4f2 = matrices.peek().getModel();
        Vec3d vec3d = camera.getPos();
        double d = vec3d.getX();
        double e = vec3d.getY();
        double f = vec3d.getZ();
        Frustum frustum2 = new Frustum(matrix4f2, matrix4f);
        frustum2.setPosition(d, e, f);
        Konas.currentFrustum = frustum2;
        Konas.EVENT_BUS.post(WorldRenderEvent.get(tickDelta));
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void afterRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        Konas.EVENT_BUS.post(LastPassEvent.get(tickDelta));
    }

    @Inject(method = "apply", at = @At("TAIL"))
    private void onLoadResourcePack(CallbackInfo ci) {
        Konas.EVENT_BUS.post(LoadResourcePackEvent.get());
    }

    @Inject(method = "onResized", at = @At("HEAD"))
    private void onResized(int width, int height, CallbackInfo ci) {
        Konas.EVENT_BUS.post(ResizeEvent.get(width, height));
    }

    @Inject(method = "drawEntityOutlinesFramebuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Framebuffer;draw(IIZ)V"))
    private void onDrawEntityOutlinesFramebuffer(CallbackInfo ci) {
        Konas.EVENT_BUS.post(DrawFramebuffersEvent.get());
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;draw()V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onDrawOutlineVertexConsumers(MatrixStack matrixStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci, Profiler profiler, Vec3d vec3d, double d, double e, double g, Matrix4f matrix4f2, boolean bl2, Frustum frustum2, boolean bl4, VertexConsumerProvider.Immediate immediate) {
        immediate.draw(KonasRenderLayers.INSTANCE.getSolidFiltered());
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;draw()V"))
    private void onRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        Konas.EVENT_BUS.post(DrawVerteciesForShadersEvent.get(tickDelta));
    }

    private boolean skip = false;

    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    private void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (skip) return;

        ShaderEntityRenderEvent event = Konas.EVENT_BUS.post(ShaderEntityRenderEvent.get(entity));

        if (event.outlineVertexConsumers != null && event.fb != null) {
            Framebuffer originalFB = this.entityOutlinesFramebuffer;
            this.entityOutlinesFramebuffer = event.fb;

            skip = true;
            renderEntity(entity, cameraX, cameraY, cameraZ, tickDelta, matrices, event.outlineVertexConsumers);
            skip = false;

            if (event.doublePass) {
                skip = true;
                ESP.meshPass = true;
                renderEntity(entity, cameraX, cameraY, cameraZ, tickDelta, matrices, event.outlineVertexConsumers);
                ESP.meshPass = false;
                skip = false;
            }

            this.entityOutlinesFramebuffer = originalFB;
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDD)V", ordinal = 0, shift = At.Shift.AFTER))
    public void afterRenderSolidLayer(MatrixStack matrixStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        WorldOutline worldOutline = (WorldOutline) ModuleManager.get(WorldOutline.class);
        if (worldOutline.isActive() && worldOutline.shaderWorks) {
            Vec3d pos = camera.getPos();
            double x = pos.x;
            double y = pos.y;
            double z = pos.z;
            renderLayer(WorldOutlineRenderLayers.INSTANCE.getSolidFiltered(), matrixStack, x, y, z);
            worldOutline.shaderOutputFB.beginWrite(false);
            renderLayer(WorldOutlineRenderLayers.INSTANCE.getSolidFiltered(), matrixStack, x, y, z);
            MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
        }

        Search search = (Search) ModuleManager.get(Search.class);
        if (!search.isActive() || !(search.fill.getValue() != Search.FillMode.None || search.outline.getValue()) || search.box.getValue() || !search.shaderWorks) return;
        Vec3d pos = camera.getPos();
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;
        renderLayer(KonasRenderLayers.INSTANCE.getSolidFiltered(), matrixStack, x, y, z);
        search.shaderOutputFB.beginWrite(false);
        renderLayer(KonasRenderLayers.INSTANCE.getSolidFiltered(), matrixStack, x, y, z);
        MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
    }

    @ModifyVariable(method = "setupTerrain", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setRenderDistanceMultiplier(D)V"), to = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOpaqueFullCube(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Z")), at = @At("STORE"), index = 20)
    public boolean modifyBl3(boolean bl3) {
        if (ModuleManager.get(Search.class).isActive()) return false;
        return bl3;
    }
}
