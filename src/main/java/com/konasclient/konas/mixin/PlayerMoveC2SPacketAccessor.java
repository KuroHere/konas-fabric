package com.konasclient.konas.mixin;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerMoveC2SPacket.class)
public interface PlayerMoveC2SPacketAccessor {
    @Accessor("onGround")
    void setOnGround(boolean ground);

    @Accessor("changePosition")
    boolean isMoving();

    @Accessor("changeLook")
    boolean isRotating();
}
