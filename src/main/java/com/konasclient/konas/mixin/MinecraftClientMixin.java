package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.client.OpenScreenEvent;
import com.konasclient.konas.event.events.player.ItemUseEvent;
import com.konasclient.konas.event.events.world.GameLeftEvent;
import com.konasclient.konas.event.events.world.UpdateEvent;
import com.konasclient.konas.util.client.ThreadUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(at = @At("HEAD"), method = "tick")
    private void onTickPre(CallbackInfo info) {
        if (ThreadUtils.canUpdate()) Konas.postEvent(UpdateEvent.Pre.get());
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void onTickPost(CallbackInfo info) {
        if (ThreadUtils.canUpdate()) Konas.postEvent(UpdateEvent.Post.get());
    }

    @Inject(at = @At("HEAD"), method = "disconnect()V")
    private void onDisconnect(CallbackInfo ci) {
        Konas.postEvent(GameLeftEvent.get());
    }

    @Inject(at = @At("HEAD"), method = "openScreen")
    private void onOpenScreen(Screen screen, CallbackInfo ci) {
        Konas.postEvent(OpenScreenEvent.get(screen));
    }

    @Redirect(method = "doItemUse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;crosshairTarget:Lnet/minecraft/util/hit/HitResult;", ordinal = 1))
    private HitResult onDoItemUse(MinecraftClient client) {
        return Konas.EVENT_BUS.post(ItemUseEvent.get(client.crosshairTarget)).crosshairTarget;
    }
}
