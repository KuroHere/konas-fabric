package com.konasclient.konas.module.modules.render;

import com.konasclient.konas.event.events.render.RenderEvent;
import com.konasclient.konas.mixin.WorldRendererAccessor;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.setting.ColorSetting;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.client.ThreadUtils;
import com.konasclient.konas.util.render.Color;
import com.konasclient.konas.util.render.geometry.BlockGeometryMasks;
import com.konasclient.konas.util.render.rendering.ModelRenderer;
import com.konasclient.konas.util.render.rendering.ShapeMode;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Interactions extends Module {
    private static final Setting<ShapeMode> mode = new Setting<>("Mode", ShapeMode.Both);
    private static final Setting<ColorSetting> fillColor = new Setting<>("Place", new ColorSetting(0x554040E8));
    private static final Setting<ColorSetting> outlineColor = new Setting<>("Outline", new ColorSetting(0xFF4040E8));

    private static final Setting<Boolean> mining = new Setting<>("Mining", true);
    private static final Setting<BreakRenderMode> breakRenderMode = new Setting<>("Animation", BreakRenderMode.Grow).withVisibility(mining::getValue);
    private static final Setting<ColorSetting> mineFillColor = new Setting<>("Mine", new ColorSetting(0x55E84040)).withVisibility(mining::getValue);
    private static final Setting<ColorSetting> mineOutlineColor = new Setting<>("MineOutline", new ColorSetting(0xFFE84040)).withVisibility(mining::getValue);

    private static final Setting<Boolean> crystals = new Setting<>("Crystals", true);
    private static final Setting<ColorSetting> crystalFillColor = new Setting<>("Crystal", new ColorSetting(0x50bf40bf)).withVisibility(crystals::getValue);
    private static final Setting<ColorSetting> crystalOutlineColor = new Setting<>("CrystalOutline", new ColorSetting(0xFFbf40bf)).withVisibility(crystals::getValue);

    private enum BreakRenderMode {
        Grow, Shrink, Cross, Static
    }

    public Interactions() {
        super("interactions", "Highlight blocks you're interacting with", 0xFF4040E8, Category.Render);
        toggle(true);
    }

    public static CopyOnWriteArrayList<Placement> crystalPlacements = new CopyOnWriteArrayList<>();

    private static ArrayList<BlockPos> placements = new ArrayList<>();

    public static void renderPlace(BlockPos pos) {
        if (ModuleManager.get(Interactions.class).isActive()) {
            placements.add(pos);
        }
    }

    @EventHandler
    public void onRender(RenderEvent event) {
        if (!ThreadUtils.canUpdate()) return;
        for (Placement crystalPlacement : crystalPlacements) {
            if (crystalPlacement.getPercentage() <= 0F) {
                crystalPlacements.remove(crystalPlacement);
            }
        }
        if (crystals.getValue()) {
           for (Placement crystalPlacement : crystalPlacements) {
               Box bb = new Box(crystalPlacement.pos);
               Color fillColor = new Color(crystalFillColor.getValue().getRed(), crystalFillColor.getValue().getGreen(), crystalFillColor.getValue().getBlue(), (int) (crystalFillColor.getValue().getAlpha() * crystalPlacement.getPercentage()));
               Color outlineColor = new Color(crystalOutlineColor.getValue().getRed(), crystalOutlineColor.getValue().getGreen(), crystalOutlineColor.getValue().getBlue(), (int) (crystalOutlineColor.getValue().getAlpha() * crystalPlacement.getPercentage()));
               renderBB(bb, crystalPlacement.direction, fillColor, outlineColor);
            }
        }
        if (mining.getValue()) {
            ((WorldRendererAccessor) mc.worldRenderer).getBlockBreakingInfos().values().forEach(info -> {
                BlockState state = mc.world.getBlockState(info.getPos());
                VoxelShape shape = state.getOutlineShape(mc.world, info.getPos());
                if (shape.isEmpty()) return;
                Box bb = shape.getBoundingBox();
                float progress = Math.min(1F, info.getStage() / 8F);

                switch (breakRenderMode.getValue()) {
                    case Grow: {
                        double amount = 0.5 - progress * 0.5;
                        renderBreakingBB(bb.expand(-amount, -amount, -amount).offset(info.getPos()));
                        break;
                    }
                    case Shrink: {
                        double amount = progress * 0.5;
                        renderBreakingBB(bb.expand(-amount, -amount, -amount).offset(info.getPos()));
                        break;
                    }
                    case Cross: {
                        double amount = 0.5 - progress * 0.5;
                        renderBreakingBB(bb.expand(-amount, -amount, -amount).offset(info.getPos()));
                        amount = progress * 0.5;
                        renderBreakingBB(bb.expand(-amount, -amount, -amount).offset(info.getPos()));
                        break;
                    }
                    default: {
                        renderBreakingBB(bb.offset(info.getPos()));
                        break;
                    }
                }
            });
        }

        for (BlockPos placement : placements) {
            int exclude = 0;
            if (placements.contains(placement.down())) exclude = exclude | BlockGeometryMasks.DOWN;
            if (placements.contains(placement.up())) exclude = exclude | BlockGeometryMasks.UP;
            if (placements.contains(placement.north())) exclude = exclude | BlockGeometryMasks.NORTH;
            if (placements.contains(placement.east())) exclude = exclude | BlockGeometryMasks.EAST;
            if (placements.contains(placement.west())) exclude = exclude | BlockGeometryMasks.WEST;
            if (placements.contains(placement.south())) exclude = exclude | BlockGeometryMasks.SOUTH;
            ModelRenderer.boxWithLines(ModelRenderer.NORMAL, ModelRenderer.LINES, placement, fillColor.getValue().getRenderColor(), outlineColor.getValue().getRenderColor(), mode.getValue(), exclude);
        }
        placements.clear();
    }

    private void renderBreakingBB(Box box) {
        ModelRenderer.boxWithLines(ModelRenderer.NORMAL, ModelRenderer.LINES, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, mineFillColor.getValue().getRenderColor(), mineOutlineColor.getValue().getRenderColor(), mode.getValue(), 0);
    }

    private void renderBB(Box bb, Direction direction, Color fillColor, Color outlineColor) {
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
        ModelRenderer.boxWithLines(ModelRenderer.NORMAL, ModelRenderer.LINES, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, fillColor,outlineColor, ShapeMode.Both, 0);
    }

    public static class Placement {
        public final BlockPos pos;
        public final Direction direction;
        private final long time;

        public Placement(BlockPos pos, Direction direction) {
            this.pos = pos;
            this.direction = direction;
            this.time = System.currentTimeMillis();
        }

        public float getPercentage() {
            long diff = System.currentTimeMillis() - time;
            if (diff > 500) {
                return MathHelper.clamp(1F - MathHelper.clamp((diff - 500) / 300F, 0F, 1F), 0F, 1F);
            } else {
                return 1F;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Placement placement = (Placement) o;
            return Objects.equals(pos, placement.pos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos);
        }
    }
}
