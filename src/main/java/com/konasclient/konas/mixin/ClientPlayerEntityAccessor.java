package com.konasclient.konas.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientPlayerEntity.class)
public interface ClientPlayerEntityAccessor {
    @Accessor(value = "autoJumpEnabled")
    void setAutoJumpEnabled(boolean autoJumpEnabled);

    @Accessor(value = "lastOnGround")
    void setLastOnGround(boolean lastOnGround);

    @Accessor(value = "lastOnGround")
    boolean getLastOnGround();

    @Accessor(value = "lastX")
    void setLastX(double lastX);

    @Accessor(value = "lastX")
    double getLastX();

    @Accessor(value = "lastBaseY")
    void setLastY(double lastY);

    @Accessor(value = "lastBaseY")
    double getLastY();

    @Accessor(value = "lastZ")
    void setLastZ(double lastZ);

    @Accessor(value = "lastZ")
    double getLastZ();

    @Accessor(value = "lastYaw")
    void setLastYaw(float lastYaw);

    @Accessor(value = "lastYaw")
    float getLastYaw();

    @Accessor(value = "lastPitch")
    void setLastPitch(float lastPitch);

    @Accessor(value = "lastPitch")
    float getLastPitch();

    @Accessor(value = "ticksSinceLastPositionPacketSent")
    void setTicksSinceLastPositionPacketSent(int ticksSinceLastPositionPacketSent);

    @Accessor(value = "ticksSinceLastPositionPacketSent")
    int getTicksSinceLastPositionPacketSent();

    @Accessor(value = "lastSneaking")
    boolean getLastSneaking();

    @Accessor(value = "lastSneaking")
    void setLastSneaking(boolean lastSneaking);

    @Accessor(value = "lastSprinting")
    boolean getLastSprinting();

    @Accessor(value = "lastSprinting")
    void setLastSprinting(boolean lastSprinting);

    @Invoker(value = "sendMovementPackets")
    void iSendMovementPackets();
}
