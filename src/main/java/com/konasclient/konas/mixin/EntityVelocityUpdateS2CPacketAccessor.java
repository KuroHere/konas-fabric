package com.konasclient.konas.mixin;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityVelocityUpdateS2CPacket.class)
public interface EntityVelocityUpdateS2CPacketAccessor {
    @Accessor(value = "velocityX")
    void setVelocityX(int VelocityX);

    @Accessor(value = "velocityX")
    int getVelocityX();

    @Accessor(value = "velocityY")
    void setVelocityY(int VelocityY);

    @Accessor(value = "velocityY")
    int getVelocityY();

    @Accessor(value = "velocityZ")
    void setVelocityZ(int VelocityZ);

    @Accessor(value = "velocityZ")
    int getVelocityZ();
}
