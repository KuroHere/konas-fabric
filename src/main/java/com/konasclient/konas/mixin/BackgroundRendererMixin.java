package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.render.RenderStatusEffectBackgroundEvent;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
    @Redirect(method = {"render(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IF)V", "applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;FZ)V"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"))
    private static boolean hasStatusEffect(LivingEntity entity, StatusEffect effect) {
        RenderStatusEffectBackgroundEvent event = Konas.EVENT_BUS.post(RenderStatusEffectBackgroundEvent.get(effect));
        if (event.isCancelled()) {
            return false;
        }

        return entity.hasStatusEffect(effect);
    }
}
