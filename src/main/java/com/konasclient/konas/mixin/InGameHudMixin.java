package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.render.PotionRenderHUDEvent;
import com.konasclient.konas.event.events.render.RenderHudEvent;
import com.konasclient.konas.event.events.render.RenderOverlayEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getArmorStack(I)Lnet/minecraft/item/ItemStack;"))
    private void onRender(MatrixStack matrixStack, float tickDelta, CallbackInfo info) {
        RenderSystem.pushMatrix();
        Konas.EVENT_BUS.post(RenderHudEvent.get(tickDelta));
        RenderSystem.popMatrix();
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    protected void renderPotionEffectsHook(MatrixStack matrices, CallbackInfo ci) {
        PotionRenderHUDEvent event = Konas.EVENT_BUS.post(PotionRenderHUDEvent.get());
        if(event.isCancelled()) ci.cancel();
    }

    @Inject(method = "renderPumpkinOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderPumpkinOverlay(CallbackInfo ci) {
        RenderOverlayEvent event = Konas.EVENT_BUS.post(RenderOverlayEvent.get(RenderOverlayEvent.Type.Pumpkin));
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

}
