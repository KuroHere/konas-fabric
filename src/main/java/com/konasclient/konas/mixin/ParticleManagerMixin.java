package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.render.ParticleEvent;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {
    @Inject(at = @At("HEAD"), method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", cancellable = true)
    public void onAddParticle(Particle particle, CallbackInfo ci) {
        ParticleEvent.AddParticle event = Konas.EVENT_BUS.post(ParticleEvent.AddParticle.get(particle));
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;)V", cancellable = true)
    public void onAddEmmiter(Entity entity, ParticleEffect particleEffect, CallbackInfo ci) {
        ParticleEvent.AddEmmiter event = Konas.EVENT_BUS.post(ParticleEvent.AddEmmiter.get(particleEffect));
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;I)V", cancellable = true)
    public void onAddEmmiterAged(Entity entity, ParticleEffect particleEffect, int maxAge, CallbackInfo ci) {
        ParticleEvent.AddEmmiter event = Konas.EVENT_BUS.post(ParticleEvent.AddEmmiter.get(particleEffect));
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
