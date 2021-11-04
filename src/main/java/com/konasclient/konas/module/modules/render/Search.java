package com.konasclient.konas.module.modules.render;

import com.konasclient.konas.event.events.client.LoadResourcePackEvent;
import com.konasclient.konas.event.events.client.ResizeEvent;
import com.konasclient.konas.event.events.render.DrawFramebuffersEvent;
import com.konasclient.konas.event.events.render.DrawVerteciesForShadersEvent;
import com.konasclient.konas.event.events.render.RenderEvent;
import com.konasclient.konas.event.events.render.WorldRenderEvent;
import com.konasclient.konas.event.events.shader.ShaderBlockRenderEvent;
import com.konasclient.konas.mixin.ShaderEffectAccessor;
import com.konasclient.konas.mixin.WorldRendererAccessor;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.BlockListSetting;
import com.konasclient.konas.setting.ColorSetting;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.render.Color;
import com.konasclient.konas.util.render.KonasOutlineVertexConsumerProvider;
import com.konasclient.konas.util.render.rendering.ModelRenderer;
import com.konasclient.konas.util.render.rendering.ShapeMode;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostProcessShader;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

public class Search extends Module {
    public static final Setting<BlockListSetting> filter = new Setting<>("Filter", new BlockListSetting());

    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x2265C3D2));

    public final Setting<Boolean> box = new Setting<>("Box", false);
    private final Setting<Boolean> tracers = new Setting<>("Tracers", false);
    private final Setting<Float> minDistance = new Setting<>("MinDistance", 50F, 250F, 0F, 5F).withVisibility(tracers::getValue);
    public final Setting<Boolean> outline = new Setting<>("Outline", true).withVisibility(() -> !box.getValue());
    private final Setting<Float> width = new Setting<>("Width", 1F, 5F, 1F, 1F).withVisibility(outline::getValue).withVisibility(() -> !box.getValue());
    public final Setting<FillMode> fill = new Setting<>("Fill", FillMode.Normal).withVisibility(() -> !box.getValue());
    private final Setting<Float> opacity = new Setting<>("Opacity", 2.5F, 10F, 0.1F, 0.1F).withVisibility(() -> fill.getValue() != FillMode.None).withVisibility(() -> !box.getValue());
    private final Setting<Float> dotSize = new Setting<>("DotSize", 0F, 10F, 0F, 1F).withVisibility(() -> !box.getValue());
    private final Setting<DotMode> dotMode = new Setting<>("DotMode", DotMode.Colored).withVisibility(() -> dotSize.getValue() > 0F && !box.getValue());
    private final Setting<Float> dotSpacing = new Setting<>("Horizontal", 8F, 20F, 1F, 1F).withVisibility(() -> dotSize.getValue() > 0F && !box.getValue());
    private final Setting<Float> vertical = new Setting<>("Vertical", 8F, 20F, 1F, 1F).withVisibility(() -> dotSize.getValue() > 0F && !box.getValue());

    public enum FillMode {
        None, Normal, Colored
    }

    public enum DotMode {
        Normal, Colored
    }

    public Search() {
        super("search", "Highlight blocks", 0xFF65C3D2, Category.Render);
    }

    private ShaderEffect outlineShader;
    public Framebuffer shaderOutputFB;

    public boolean shaderWorks = false;

    private OutlineVertexConsumerProvider outlineVertexConsumers;

    private CopyOnWriteArrayList<BlockVec> blocks = new CopyOnWriteArrayList<>();

    public void onEnable() {
        if (!shaderWorks) {
            setupShaders();
        }
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }
    }

    private boolean isValid(Block block) {
        return filter.getValue().getBlocks().contains(block);
    }

    @EventHandler
    public void onBlockRender(ShaderBlockRenderEvent event) {
        if (isValid(event.state.getBlock())) {
            BlockVec vec = new BlockVec(event.pos.getX(), event.pos.getY(), event.pos.getZ());
            if (!blocks.contains(vec)) {
                blocks.add(vec);
            }
            event.cancel();
        }
    }

    @EventHandler
    public void onRender(RenderEvent event) {
        if (!box.getValue()) return;

        Color clr = color.getValue().getRenderColor();
        Color outlineClr = new Color(clr.r, clr.g, clr.b, 255);

        for (BlockVec vec : blocks) {
            BlockPos pos = new BlockPos(vec.x, vec.y, vec.z);
            BlockState state = mc.world.getBlockState(pos);
            if (!isValid(state.getBlock())) {
                blocks.remove(vec);
                continue;
            }
            VoxelShape shape = state.getOutlineShape(mc.world, pos);
            if (shape.isEmpty()) continue;
            Box bb = shape.getBoundingBox().offset(pos);
            ModelRenderer.boxWithLines(ModelRenderer.NORMAL, ModelRenderer.LINES, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, clr, outlineClr, ShapeMode.Both, 0);
            if (tracers.getValue()) {
                Vec3d center = new Vec3d(bb.minX + (bb.maxX - bb.minX) * 0.5, bb.minY + (bb.maxY - bb.minY) * 0.5, bb.minZ + (bb.maxZ - bb.minZ) * 0.5);
                if (mc.player.getPos().distanceTo(center) >= minDistance.getValue()) {
                    renderTracer(center, outlineClr);
                }
            }
        }
    }

    private void renderTracer(Vec3d vec, Color clr) {
        Vec3d cam = new Vec3d(0, 0, 100).rotateX(-(float) Math.toRadians(mc.gameRenderer.getCamera().getPitch())).rotateY(-(float) Math.toRadians(mc.gameRenderer.getCamera().getYaw())).add(mc.gameRenderer.getCamera().getPos());
        ModelRenderer.LINES.line(vec.x, vec.y, vec.z, cam.x, cam.y, cam.z, clr);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (shaderWorks && outlineShader != null && (outline.getValue() || fill.getValue() != FillMode.None)) {
            for (PostProcessShader postProcessShader : ((ShaderEffectAccessor) outlineShader).getPasses()) {
                postProcessShader.getProgram().getUniformByNameOrDummy("outlineColor").set(color.getValue().getRed() / 255F, color.getValue().getGreen() / 255F, color.getValue().getBlue() / 255F, outline.getValue() ? 1F : 0F);
                postProcessShader.getProgram().getUniformByNameOrDummy("filledColor").set(color.getValue().getRed() / 255F, color.getValue().getGreen() / 255F, color.getValue().getBlue() / 255F, fill.getValue() != FillMode.None ? opacity.getValue() / 10F : 0F);
                postProcessShader.getProgram().getUniformByNameOrDummy("width").set(width.getValue());
                postProcessShader.getProgram().getUniformByNameOrDummy("dotSpacing").set(dotSpacing.getValue());
                postProcessShader.getProgram().getUniformByNameOrDummy("vertical").set(vertical.getValue());
                postProcessShader.getProgram().getUniformByNameOrDummy("dotSize").set(dotSize.getValue());
                postProcessShader.getProgram().getUniformByNameOrDummy("fillMode").set(fill.getValue().ordinal());
                postProcessShader.getProgram().getUniformByNameOrDummy("dotMode").set(dotMode.getValue().ordinal());
            }
            mc.worldRenderer.loadEntityOutlineShader();
            shaderOutputFB.clear(mc.IS_SYSTEM_MAC);
            mc.getFramebuffer().beginWrite(false);
        }
    }

    @EventHandler
    public void onDrawVerteciesForShaders(DrawVerteciesForShadersEvent event) {
        if (shaderWorks && outlineShader != null && (outline.getValue() || fill.getValue() != FillMode.None)) {
            // OutlineVertexConsumerProvider draws to world entity outlines fb, we can replace it with our fb
            Framebuffer original = mc.worldRenderer.getEntityOutlinesFramebuffer();
            ((WorldRendererAccessor) mc.worldRenderer).setEntityOutlinesFramebuffer(shaderOutputFB);

            outlineVertexConsumers.draw();

            ((WorldRendererAccessor) mc.worldRenderer).setEntityOutlinesFramebuffer(original);

            outlineShader.render(event.tickDelta);

            mc.getFramebuffer().beginWrite(false); // vanilla does this idk why
        }
    }

    @EventHandler
    public void onDrawFramebuffers(DrawFramebuffersEvent event) {
        if (shaderWorks && outlineShader != null && (outline.getValue() || fill.getValue() != FillMode.None)) {
            // bool disables blending if true
            shaderOutputFB.draw(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(), false);
        }
    }

    @EventHandler
    public void onLoadResourcePack(LoadResourcePackEvent event) {
        setupShaders();
    }

    private void setupShaders() {
        resetShaders();

        try {
            outlineShader = new ShaderEffect(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), new Identifier("shaders/post/other_outline_shader.json"));
            shaderWorks = true;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error creating shader!");
            shaderWorks = false;
            return;
        }

        updateDimensions(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
        shaderOutputFB = outlineShader.getSecondaryTarget("final");
        outlineVertexConsumers = new KonasOutlineVertexConsumerProvider(mc.getBufferBuilders().getEntityVertexConsumers());
    }

    @EventHandler
    public void onResize(ResizeEvent event) {
        updateDimensions(event.width, event.height);
    }

    public void updateDimensions(int width, int height) {
        if (outlineShader != null) {
            // TODO: Make accessor and check if width/height are the same
            outlineShader.setupDimensions(width, height);
        }
    }

    private void resetShaders() {
        if (outlineShader != null) {
            try {
                outlineShader.close();
            } catch (Exception e) {
                System.err.println("Error resseting shader!");
            }
            outlineShader = null;
            shaderWorks = false;
        }
    }

    private static class BlockVec {
        public final double x;
        public final double y;
        public final double z;

        public BlockVec(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public boolean equals(Object object) {
            if (object instanceof BlockVec) {
                BlockVec v = (BlockVec) object;
                return Double.compare(x, v.x) == 0 && Double.compare(y, v.y) == 0 && Double.compare(z, v.z) == 0;
            }
            return super.equals(object);
        }

        public double getDistance(BlockVec v) {
            double dx = x - v.x;
            double dy = y - v.y;
            double dz = z - v.z;

            return Math.sqrt(dx*dx + dy*dy + dz*dz);
        }
    }
}
