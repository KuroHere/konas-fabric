package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.player.PlayerPushEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin {

    @Shadow
    public float yaw;

    @Shadow
    public void addVelocity(double deltaX, double deltaY, double deltaZ) {}

    @Redirect(method = "pushAwayFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    private void onPushAwayFrom(Entity entity, double deltaX, double deltaY, double deltaZ) {
        if (entity == MinecraftClient.getInstance().player) {
            PlayerPushEvent event = Konas.EVENT_BUS.post(PlayerPushEvent.get(new Vec3d(deltaX, deltaY, deltaZ)));

            addVelocity(event.movement.x, event.movement.y, event.movement.z);
        }
    }
}
