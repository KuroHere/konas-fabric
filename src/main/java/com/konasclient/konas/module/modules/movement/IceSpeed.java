package com.konasclient.konas.module.modules.movement;

import com.konasclient.konas.mixin.AbstractBlockAccessor;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import net.minecraft.block.Blocks;

public class IceSpeed extends Module {

    public IceSpeed() {
        super("ice-speed", 0xFF36D2DF, Category.Movement);
    }

    @Override
    public void onEnable() {
        ((AbstractBlockAccessor) Blocks.ICE).setSlipperiness(0.4F);
        ((AbstractBlockAccessor) Blocks.FROSTED_ICE).setSlipperiness(0.4F);
        ((AbstractBlockAccessor) Blocks.PACKED_ICE).setSlipperiness(0.4F);
        ((AbstractBlockAccessor) Blocks.BLUE_ICE).setSlipperiness(0.4F);
    }

    public void onDisable() {
        ((AbstractBlockAccessor) Blocks.ICE).setSlipperiness(0.98F);
        ((AbstractBlockAccessor) Blocks.FROSTED_ICE).setSlipperiness(0.98F);
        ((AbstractBlockAccessor) Blocks.PACKED_ICE).setSlipperiness(0.98F);
        ((AbstractBlockAccessor) Blocks.BLUE_ICE).setSlipperiness(0.98F);
    }

}
