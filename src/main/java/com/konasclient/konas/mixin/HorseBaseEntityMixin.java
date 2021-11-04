package com.konasclient.konas.mixin;

import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.exploit.EntityControl;
import com.konasclient.konas.module.modules.misc.AutoMount;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HorseBaseEntity.class)
public abstract class HorseBaseEntityMixin extends AnimalEntity {
    @Shadow @Nullable public abstract Entity getPrimaryPassenger();

    protected HorseBaseEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "isSaddled", at = @At("HEAD"), cancellable = true)
    public void onIsSaddled(CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.get(EntityControl.class).isActive()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canBeControlledByRider", at = @At("HEAD"), cancellable = true)
    public void onCanBeControlledByRider(CallbackInfoReturnable<Boolean> cir) {
        if (this.hasPassengers() && this.getPrimaryPassenger().equals(MinecraftClient.getInstance().player)) {
            if (ModuleManager.get(EntityControl.class).isActive()) {
                cir.setReturnValue(true);
            }
        }
    }
}
