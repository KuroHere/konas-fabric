package com.konasclient.konas.event.events.render;

import com.konasclient.konas.event.Cancellable;
import net.minecraft.client.particle.Particle;
import net.minecraft.particle.ParticleEffect;

public class ParticleEvent extends Cancellable {
    public static class AddParticle extends ParticleEvent {
        private static final AddParticle INSTANCE = new AddParticle();

        public Particle particle;

        public static AddParticle get(Particle particle) {
            INSTANCE.particle = particle;
            INSTANCE.setCancelled(false);
            return INSTANCE;
        }
    }

    public static class AddEmmiter extends ParticleEvent {
        private static final AddEmmiter INSTANCE = new AddEmmiter();

        public ParticleEffect emmiter;

        public static AddEmmiter get(ParticleEffect emmiter) {
            INSTANCE.emmiter = emmiter;
            INSTANCE.setCancelled(false);
            return INSTANCE;
        }
    }
}
