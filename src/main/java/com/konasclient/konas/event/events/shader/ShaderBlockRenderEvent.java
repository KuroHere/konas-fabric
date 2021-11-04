package com.konasclient.konas.event.events.shader;

import com.konasclient.konas.event.Cancellable;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class ShaderBlockRenderEvent extends Cancellable {
    private static ShaderBlockRenderEvent INSTANCE = new ShaderBlockRenderEvent();

    public BlockPos pos;
    public BlockState state;

    public static ShaderBlockRenderEvent get(BlockPos pos, BlockState state) {
        INSTANCE.pos = pos;
        INSTANCE.state = state;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
