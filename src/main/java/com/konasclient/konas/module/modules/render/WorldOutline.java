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
import com.konasclient.konas.setting.ColorSetting;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.render.KonasOutlineVertexConsumerProvider;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostProcessShader;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.util.Identifier;

import java.io.IOException;

public class WorldOutline extends Module {
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0xFF36B50A));
    private final Setting<Float> width = new Setting<>("Width", 1F, 5F, 1F, 1F);

    public WorldOutline() {
        super("world-outline", "Draws an outline around the world", 0xFF36B50A, Category.Render);
    }

    public void onEnable() {
        if (!shaderWorks) {
            setupShaders();
        }
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }
    }

    private ShaderEffect outlineShader;
    public Framebuffer shaderOutputFB;

    public boolean shaderWorks = false;

    private OutlineVertexConsumerProvider outlineVertexConsumers;

    public static boolean check(BlockState blockState) {
        Block blk = blockState.getBlock();
        return blk != null && blk != Blocks.ACACIA_LEAVES &&
                blk != Blocks.SPRUCE_LEAVES &&
                blk != Blocks.BIRCH_LEAVES &&
                blk != Blocks.DARK_OAK_LEAVES &&
                blk != Blocks.OAK_LEAVES &&
                blk != Blocks.JUNGLE_LEAVES &&
                blk != Blocks.TALL_GRASS &&
                blk != Blocks.GRASS &&
                blk != Blocks.SUNFLOWER &&
                blk != Blocks.DEAD_BUSH &&
                blk != Blocks.REDSTONE_WIRE;
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (shaderWorks && outlineShader != null) {
            for (PostProcessShader postProcessShader : ((ShaderEffectAccessor) outlineShader).getPasses()) {
                postProcessShader.getProgram().getUniformByNameOrDummy("outlineColor").set(color.getValue().getRed() / 255F, color.getValue().getGreen() / 255F, color.getValue().getBlue() / 255F, color.getValue().getAlpha() / 255F);
                postProcessShader.getProgram().getUniformByNameOrDummy("width").set(width.getValue());
            }
            mc.worldRenderer.loadEntityOutlineShader();
            shaderOutputFB.clear(mc.IS_SYSTEM_MAC);
            mc.getFramebuffer().beginWrite(false);
        }
    }

    @EventHandler
    public void onDrawVerteciesForShaders(DrawVerteciesForShadersEvent event) {
        if (shaderWorks && outlineShader != null) {
            // OutlineVertexConsumerProvider draws to world entity outlines fb, we can replace it with our fb
            Framebuffer original = mc.worldRenderer.getEntityOutlinesFramebuffer();
            ((WorldRendererAccessor) mc.worldRenderer).setEntityOutlinesFramebuffer(shaderOutputFB);

            outlineVertexConsumers.draw();

            ((WorldRendererAccessor) mc.worldRenderer).setEntityOutlinesFramebuffer(original);

            outlineShader.render(event.tickDelta);

            mc.getFramebuffer().beginWrite(false);
        }
    }

    @EventHandler
    public void onDrawFramebuffers(DrawFramebuffersEvent event) {
        if (shaderWorks && outlineShader != null) {
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
            outlineShader = new ShaderEffect(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), new Identifier("shaders/post/world_outline_shader.json"));
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
}
