package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.network.PacketEvent;
import com.konasclient.konas.interfaceaccessors.IClientConnection;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin implements IClientConnection {
    @Shadow protected abstract void sendQueuedPackets();

    @Shadow public abstract boolean isOpen();

    @Shadow protected abstract void sendImmediately(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> callback);

    @Shadow @Final private Queue<ClientConnection.QueuedPacket> packetQueue;

    @Inject(method = "send(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacketHead(Packet<?> packet, CallbackInfo ci) {
        PacketEvent.Send event = Konas.EVENT_BUS.post(PacketEvent.Send.get(packet));

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void onHandlePacket(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        PacketEvent.Receive event = Konas.EVENT_BUS.post(PacketEvent.Receive.get(packet));

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Override
    public void sendDirectly(Packet<?> packet) {
        if (this.isOpen()) {
            this.sendQueuedPackets();
            this.sendImmediately(packet, null);
        } else {
            this.packetQueue.add(new ClientConnection.QueuedPacket(packet, null));
        }
    }
}
