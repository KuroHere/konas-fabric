package com.konasclient.konas.mixin;

import com.konasclient.konas.util.math.DamageCalculator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.World.isOutOfBuildLimitVertically;

@Mixin(World.class)
public class WorldMixin {
    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    public void onGetBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (DamageCalculator.terrainIgnore) {
            if (isOutOfBuildLimitVertically(pos)) {
                return;
            } else {
                WorldChunk worldChunk = MinecraftClient.getInstance().world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
                BlockState tempState = worldChunk.getBlockState(pos);
                if (tempState.getBlock() == Blocks.OBSIDIAN || tempState.getBlock() == Blocks.BEDROCK || tempState.getBlock() == Blocks.ENDER_CHEST || tempState.getBlock() == Blocks.RESPAWN_ANCHOR) return;
                cir.setReturnValue(Blocks.AIR.getDefaultState());
            }
        }
    }
}
