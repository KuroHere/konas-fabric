package com.konasclient.konas.mixin;

import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.player.NoRotate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Redirect(method = "onPlayerPositionLook", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;updatePositionAndAngles(DDDFF)V"))
    public void onSetPositionAngAngles(PlayerEntity playerEntity, double x, double y, double z, float yaw, float pitch) {
        if (MinecraftClient.getInstance().player != null && playerEntity == MinecraftClient.getInstance().player && ModuleManager.get(NoRotate.class).isActive() && NoRotate.strict.getValue()) {
            playerEntity.updatePositionAndAngles(x, y, z, playerEntity.yaw, playerEntity.pitch);
        } else {
            playerEntity.updatePositionAndAngles(x, y, z, yaw, pitch);
        }
    }
}
