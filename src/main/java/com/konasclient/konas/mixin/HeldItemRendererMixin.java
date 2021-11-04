package com.konasclient.konas.mixin;

import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.render.OldAnimations;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Redirect(method = "updateHeldItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAttackCooldownProgress(F)F"))
    public float onGetAttackCooldownProgress(ClientPlayerEntity clientPlayerEntity, float baseTime) {
        if (ModuleManager.get(OldAnimations.class).isActive()) return 1F;
        return clientPlayerEntity.getAttackCooldownProgress(baseTime);
    }
}
