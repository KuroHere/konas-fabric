package com.konasclient.konas.mixin;

import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DimensionType.class)
public interface DimensionTypeAccesor {
    @Accessor(value = "field_24767")
    void setfield_24767(float[] field_24767);

    @Accessor(value = "field_24767")
    float[] getfield_24767();

    @Accessor(value = "ambientLight")
    float getAmbientLight();

}
