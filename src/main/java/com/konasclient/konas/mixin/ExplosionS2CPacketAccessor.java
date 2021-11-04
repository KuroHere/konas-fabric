package com.konasclient.konas.mixin;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExplosionS2CPacket.class)
public interface ExplosionS2CPacketAccessor {
    @Accessor(value = "playerVelocityX")
    void setPlayerVelocityX(float playerVelocityX);

    @Accessor(value = "playerVelocityX")
    float getPlayerVelocityX();

    @Accessor(value = "playerVelocityY")
    void setPlayerVelocityY(float playerVelocityY);

    @Accessor(value = "playerVelocityY")
    float getPlayerVelocityY();

    @Accessor(value = "playerVelocityZ")
    void setPlayerVelocityZ(float playerVelocityZ);

    @Accessor(value = "playerVelocityZ")
    float getPlayerVelocityZ();
}
