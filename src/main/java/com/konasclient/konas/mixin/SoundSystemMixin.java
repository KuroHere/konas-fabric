package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.sound.SoundEvent;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.client.sound.TickableSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    public void play(SoundInstance soundInstance, CallbackInfo ci) {
        SoundEvent event = Konas.EVENT_BUS.post(SoundEvent.get(soundInstance));

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;I)V", at = @At("HEAD"), cancellable = true)
    public void play(SoundInstance soundInstance, int delay, CallbackInfo ci) {
        SoundEvent event = Konas.EVENT_BUS.post(SoundEvent.get(soundInstance));

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "playNextTick", at = @At("HEAD"), cancellable = true)
    public void playNextTick(TickableSoundInstance sound, CallbackInfo ci) {
        SoundEvent event = Konas.EVENT_BUS.post(SoundEvent.get(sound));

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}