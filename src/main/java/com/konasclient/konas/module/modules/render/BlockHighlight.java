package com.konasclient.konas.module.modules.render;

import com.konasclient.konas.event.events.render.RenderEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.ColorSetting;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.render.rendering.ModelRenderer;
import com.konasclient.konas.util.render.rendering.ShapeMode;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class BlockHighlight extends Module {
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Normal);
    private final Setting<ShapeMode> shape = new Setting<>("Shape", ShapeMode.Both);
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x2250b4b4));
    private final Setting<ColorSetting> outline = new Setting<>("Outline", new ColorSetting(0xFF50b4b4));

    private enum Mode {
        Normal, Flat, Complex
    }

    public BlockHighlight() {
        super("block-highlight", "Highlights the block you're currently looking at", 0x2250b4b4, Category.Render);
    }

    @EventHandler
    public void onRender(RenderEvent event) {
        if (mc.crosshairTarget == null || !(mc.crosshairTarget instanceof BlockHitResult)) return;

        BlockHitResult result = (BlockHitResult) mc.crosshairTarget;

        BlockPos pos = result.getBlockPos();
        Direction direction = result.getSide();

        BlockState state = mc.world.getBlockState(pos);
        VoxelShape shape = state.getOutlineShape(mc.world, pos);
        if (shape.isEmpty()) return;
        if (mode.getValue() == Mode.Complex) {
            for (Box bb : shape.getBoundingBoxes()) {
                renderBB(bb.offset(pos), null);
            }
        } else {
            Box bb = shape.getBoundingBox();

            renderBB(bb.offset(pos), mode.getValue() == Mode.Flat ? direction : null);
        }
    }

    private void renderBB(Box bb, Direction direction) {
        if (direction != null) {
            switch (direction) {
                case DOWN:
                    bb = new Box(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.maxZ);
                    break;
                case UP:
                    bb = new Box(bb.minX, bb.maxY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
                    break;
                case NORTH:
                    bb = new Box(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.minZ);
                    break;
                case SOUTH:
                    bb = new Box(bb.minX, bb.minY, bb.maxZ, bb.maxX, bb.maxY, bb.maxZ);
                    break;
                case EAST:
                    bb = new Box(bb.maxX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
                    break;
                case WEST:
                    bb = new Box(bb.minX, bb.minY, bb.minZ, bb.minX, bb.maxY, bb.maxZ);
                    break;
            }
        }
        ModelRenderer.boxWithLines(ModelRenderer.NORMAL, ModelRenderer.LINES, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, color.getValue().getRenderColor(), outline.getValue().getRenderColor(), shape.getValue(), 0);
    }
}
