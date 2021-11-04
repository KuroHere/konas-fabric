package com.konasclient.konas.interfaceaccessors;

import net.minecraft.network.Packet;

public interface IClientConnection {
    void sendDirectly(Packet<?> packet);
}
