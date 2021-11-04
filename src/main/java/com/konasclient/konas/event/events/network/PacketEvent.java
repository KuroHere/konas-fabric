package com.konasclient.konas.event.events.network;

import com.konasclient.konas.event.Cancellable;
import net.minecraft.network.Packet;

public class PacketEvent extends Cancellable {
    public Packet<?> packet;

    public static class Send extends PacketEvent {
        private static final Send INSTANCE = new Send();

        public static Send get(Packet<?> packet) {
            INSTANCE.packet = packet;
            INSTANCE.setCancelled(false);
            return INSTANCE;
        }
    }

    public static class Receive extends PacketEvent {
        private static final Receive INSTANCE = new Receive();

        public static Receive get(Packet<?> packet) {
            INSTANCE.packet = packet;
            INSTANCE.setCancelled(false);
            return INSTANCE;
        }
    }
}
