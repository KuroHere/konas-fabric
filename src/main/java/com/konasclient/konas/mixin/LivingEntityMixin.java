package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.player.JumpEvent;
import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.player.AntiLevitation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin extends EntityMixin {

    @Redirect(method={"travel"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"))
    public boolean onPotionCheck(LivingEntity livingEntity, StatusEffect effect) {
        if (ModuleManager.get(AntiLevitation.class).isActive() && livingEntity == MinecraftClient.getInstance().player && effect == StatusEffects.LEVITATION) {
            return false;
        }
        return livingEntity.hasStatusEffect(effect);
    }

    private float jumpYaw;

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void preJump(CallbackInfo info) {
        if((Object)this == MinecraftClient.getInstance().player) {
            JumpEvent event = Konas.postEvent(JumpEvent.get(jumpYaw = this.yaw));
            this.yaw = event.yaw;

            if (event.isCancelled()) info.cancel();

        }
    }

    @Inject(method = "jump", at = @At("TAIL"))
    private void postJump(CallbackInfo info) {
        if((Object)this == MinecraftClient.getInstance().player) {
            this.yaw = jumpYaw;
        }
    }
}
