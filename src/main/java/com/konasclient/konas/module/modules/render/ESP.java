package com.konasclient.konas.module.modules.render;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.client.LoadResourcePackEvent;
import com.konasclient.konas.event.events.client.ResizeEvent;
import com.konasclient.konas.event.events.render.*;
import com.konasclient.konas.event.events.shader.ShaderEntityRenderEvent;
import com.konasclient.konas.mixin.ShaderEffectAccessor;
import com.konasclient.konas.mixin.WorldRendererAccessor;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.ColorSetting;
import com.konasclient.konas.setting.Parent;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.friend.Friends;
import com.konasclient.konas.util.render.Color;
import com.konasclient.konas.util.render.KonasOutlineVertexConsumerProvider;
import com.konasclient.konas.util.render.mesh.DrawMode;
import com.konasclient.konas.util.render.mesh.MeshBuilder;
import com.konasclient.konas.util.render.rendering.ModelRenderer;
import com.konasclient.konas.util.render.rendering.ShapeMode;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.*;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostProcessShader;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;

public class ESP extends Module {
    private final Setting<Boolean> box = new Setting<>("Box", false);
    private final Setting<Boolean> outline = new Setting<>("Outline", true);
    private final Setting<Float> width = new Setting<>("Width", 1F, 5F, 1F, 1F).withVisibility(outline::getValue);
    private final Setting<Boolean> fill = new Setting<>("Fill", false);
    private final Setting<Float> opacity = new Setting<>("Opacity", 2.5F, 10F, 0.1F, 0.1F).withVisibility(fill::getValue);
    private final Setting<Float> dotSize = new Setting<>("DotSize", 0F, 10F, 0F, 1F);
    private final Setting<Float> dotSpacing = new Setting<>("Horizontal", 8F, 20F, 1F, 1F).withVisibility(() -> dotSize.getValue() > 0F);
    private final Setting<Float> vertical = new Setting<>("Vertical", 8F, 20F, 1F, 1F).withVisibility(() -> dotSize.getValue() > 0F);

    private final Setting<Parent> selection = new Setting<>("Selection", new Parent(false));
    private final Setting<Boolean> players = new Setting<>("Players", true).withParent(selection);
    private final Setting<Boolean> friends = new Setting<>("Friends", true).withParent(selection);
    private final Setting<Boolean> crystals = new Setting<>("Crystals", true).withParent(selection);
    private final Setting<Boolean> items = new Setting<>("Items", false).withParent(selection);
    private final Setting<Boolean> creatures = new Setting<>("Creatures", false).withParent(selection);
    private final Setting<Boolean> monsters = new Setting<>("Monsters", false).withParent(selection);
    private final Setting<Boolean> ambients = new Setting<>("Ambients", false).withParent(selection);

    private final Setting<Parent> colors = new Setting<>("Colors", new Parent(false));
    private final Setting<ColorSetting> player = new Setting<>("Player", new ColorSetting(0x403BCBCD)).withParent(colors);
    private final Setting<ColorSetting> friend = new Setting<>("Friend", new ColorSetting(0x4016E316)).withParent(colors);
    private final Setting<ColorSetting> crystal = new Setting<>("Crystal", new ColorSetting(0x40A4219C)).withParent(colors);
    private final Setting<ColorSetting> item = new Setting<>("Item", new ColorSetting(0x40D1DA1A)).withParent(colors);
    private final Setting<ColorSetting> creature = new Setting<>("Creature", new ColorSetting(0x4023E316)).withParent(colors);
    private final Setting<ColorSetting> monster = new Setting<>("Monster", new ColorSetting(0x40E31616)).withParent(colors);
    private final Setting<ColorSetting> ambient = new Setting<>("Ambient", new ColorSetting(0x4016E374)).withParent(colors);

    private final static Setting<Parent> lines = new Setting<>("Lines", new Parent(false));
    private static final Setting<Boolean> normal = new Setting<>("Normal", false).withParent(lines);
    private static final Setting<Float> lineWidth = new Setting<>("LineWidth", 1F, 5F, 0.1F, 0.1F).withParent(lines);
    private static final Setting<Boolean> filled = new Setting<>("Filled", false).withParent(lines);
    private static final Setting<Boolean> dotted = new Setting<>("Dotted", false).withParent(lines);
    private static final Setting<Double> size = new Setting<>("Size", 1D, 10D, 0.5D, 0.1D).withParent(lines);
    private static final Setting<Boolean> playerLines = new Setting<>("PlayerLines", false).withParent(lines);
    private static final Setting<Boolean> crystalLines = new Setting<>("CrystalLines", false).withParent(lines);
    private static final Setting<Boolean> otherLines = new Setting<>("OtherLines", false).withParent(lines);

    private final Setting<Parent> storages = new Setting<>("Storages", new Parent(false));
    private final Setting<ShapeMode> shapeMode = new Setting<>("Mode", ShapeMode.Both).withParent(storages);
    private final Setting<Boolean> tracers = new Setting<>("Tracers", false).withParent(storages);
    private final Setting<Float> shulkers = new Setting<>("Shulkers", 0F, 1F, 0F, 0.05F).withParent(storages);
    private final Setting<ColorSetting> chests = new Setting<>("Chests", new ColorSetting(0x00C8C640)).withParent(storages);
    private final Setting<ColorSetting> trapped = new Setting<>("Trapped", new ColorSetting(0x00C89F40)).withParent(storages);
    private final Setting<ColorSetting> ender = new Setting<>("Ender", new ColorSetting(0x000BAB00)).withParent(storages);
    private final Setting<ColorSetting> barrels = new Setting<>("Barrels", new ColorSetting(0x0057400B)).withParent(storages);
    private final Setting<ColorSetting> other = new Setting<>("Others", new ColorSetting(0x00646B61)).withParent(storages);

    public ESP() {
        super("ESP", "Highlights entities", 0xFF3BCBCD, Category.Render);
    }

    private ShaderEffect outlineShader;
    public Framebuffer shaderOutputFB;

    private boolean shaderWorks = false;

    private OutlineVertexConsumerProvider outlineVertexConsumers;

    private static Color lastColor = null;

    private static MeshBuilder meshBuilder = new MeshBuilder();
    private static MeshBuilder fillMeshBuilder = new MeshBuilder();

    private static MeshBuilder dotMeshBuilder = new MeshBuilder();

    public static boolean meshPass = false;

    private static int i = 0;

    public static void putVertex(double x, double y, double z) {
        if (lastColor != null) {
            if (i == 0) {
                if (normal.getValue()) {
                    meshBuilder.lineWidth = lineWidth.getValue();
                    meshBuilder.noOffset = true;
                    meshBuilder.begin(null, DrawMode.LineLoop, VertexFormats.POSITION_COLOR);
                }
                if (filled.getValue()) {
                    fillMeshBuilder.noOffset = true;
                    fillMeshBuilder.begin(null, DrawMode.Quads, VertexFormats.POSITION_COLOR);
                }
                if (dotted.getValue()) {
                    dotMeshBuilder.noOffset = true;
                    dotMeshBuilder.begin(null, DrawMode.Triangles, VertexFormats.POSITION_COLOR);
                }
            }
            Color lineColor = new Color(lastColor.r, lastColor.g, lastColor.b, 255);
            if (normal.getValue()) {
                meshBuilder.pos(x, y, z).color(lineColor).endVertex();
            }
            if (filled.getValue()) {
                fillMeshBuilder.pos(x, y, z).color(lastColor).endVertex();
            }
            if (dotted.getValue()) {

                dotMeshBuilder.boxSides(x - 0.01 * size.getValue(), y - 0.01 * size.getValue(), z - 0.01 * size.getValue(), x + 0.01 * size.getValue(), y + 0.01 * size.getValue(), z + 0.01 * size.getValue(), lineColor, 0);
            }
            i++;
            if (i == 4) {
                i = 0;
                if (normal.getValue()) {
                    meshBuilder.end();
                }
                if (filled.getValue()) {
                    fillMeshBuilder.end();
                }
                if (dotted.getValue()) {
                    dotMeshBuilder.end();
                }
            }
        }
    }

    public void onEnable() {
        i = 0;
        if (!shaderWorks) {
            setupShaders();
        }
    }

    private boolean shouldRender(Entity entity) {
        if (entity == null) {
            return false;
        }

        if (Konas.currentFrustum != null) {
            if (!Konas.currentFrustum.isVisible(entity.getBoundingBox())) {
                return false;
            }
        }

        if (entity instanceof PlayerEntity) {
            if (entity == mc.player) return false;

            if (Friends.isFriend(entity.getName().asString())) {
                return friends.getValue();
            }

            return players.getValue();
        }

        if (entity instanceof EndCrystalEntity) {
            return crystals.getValue();
        }

        if (entity instanceof ItemEntity) {
            return items.getValue();
        }

        switch (entity.getType().getSpawnGroup()) {
            case CREATURE:
            case WATER_AMBIENT:
            case WATER_CREATURE: return creatures.getValue();
            case MONSTER: return monsters.getValue();
            case AMBIENT: return ambients.getValue();
            default: return false;
        }
    }

    private ColorSetting getEntityColor(Entity entity) {
        if (entity instanceof PlayerEntity) {
            if (Friends.isFriend(entity.getName().asString())) {
                return friend.getValue();
            }

            return player.getValue();
        }

        if (entity instanceof EndCrystalEntity) {
            return crystal.getValue();
        }

        if (entity instanceof ItemEntity) {
            return item.getValue();
        }

        switch (entity.getType().getSpawnGroup()) {
            case CREATURE:
            case WATER_AMBIENT:
            case WATER_CREATURE: return creature.getValue();
            case MONSTER: return monster.getValue();
            case AMBIENT: return ambient.getValue();
            default: return new ColorSetting(0xFFFFFFFF);
        }
    }

    @EventHandler
    public void onRender(RenderEvent event) {
        if (box.getValue()) {
            for (Entity entity : mc.world.getEntities()) {
                if (shouldRender(entity)) {
                    Box bb = entity.getBoundingBox();
                    ColorSetting clr = getEntityColor(entity);
                    ModelRenderer.boxWithLines(ModelRenderer.NORMAL, ModelRenderer.LINES, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, clr.getRenderColor(), new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), 255), ShapeMode.Both, 0);
                }
            }
        }

        for (BlockEntity blockEntity : mc.world.blockEntities) {
            Color color = null;

            if (blockEntity instanceof TrappedChestBlockEntity) {
                color = trapped.getValue().getRenderColor();
            } else if (blockEntity instanceof ChestBlockEntity) {
                color = chests.getValue().getRenderColor();
            } else if (blockEntity instanceof EnderChestBlockEntity) {
                color = ender.getValue().getRenderColor();
            } else if (blockEntity instanceof BarrelBlockEntity) {
                color = barrels.getValue().getRenderColor();
            } else if (shulkers.getValue() > 0F && blockEntity instanceof ShulkerBoxBlockEntity) {
                color = new Color(((ShulkerBoxBlockEntity) blockEntity).getColor().getFireworkColor());
                color.a = (int) (shulkers.getValue() * 255F);
            } else if (blockEntity instanceof AbstractFurnaceBlockEntity || blockEntity instanceof DispenserBlockEntity || blockEntity instanceof HopperBlockEntity) {
                color = other.getValue().getRenderColor();
            }

            if (color == null || color.a == 0) continue;

            Color outlineColor = new Color(color.r, color.g, color.b, 255);

            ModelRenderer.boxWithLines(ModelRenderer.NORMAL, ModelRenderer.LINES, blockEntity.getPos(), color, outlineColor, shapeMode.getValue(), 0);

            if (tracers.getValue()) {
                Vec3d camPos = new Vec3d(0, 0, 50).rotateX(-(float) Math.toRadians(mc.gameRenderer.getCamera().getPitch())).rotateY(-(float) Math.toRadians(mc.gameRenderer.getCamera().getYaw())).add(mc.gameRenderer.getCamera().getPos());
                ModelRenderer.LINES.line(camPos.x, camPos.y, camPos.z, blockEntity.getPos().getX() + 0.5, blockEntity.getPos().getY() + 0.5, blockEntity.getPos().getZ() + 0.5, outlineColor);
            }
        }
    }

    @EventHandler
    public void onShaderRenderEntity(ShaderEntityRenderEvent event) {
        if (shaderWorks && outlineShader != null && (outline.getValue() || fill.getValue() || playerLines.getValue() || crystalLines.getValue() || otherLines.getValue())) {
            i = 0;
            if (shouldRender(event.entity)) {
                event.fb = shaderOutputFB;
                event.outlineVertexConsumers = outlineVertexConsumers;
                lastColor = null;
                ColorSetting color = getEntityColor(event.entity);
                if ((playerLines.getValue() && event.entity instanceof PlayerEntity) || (crystalLines.getValue() && event.entity instanceof EndCrystalEntity) || ((otherLines.getValue() && !(event.entity instanceof PlayerEntity || event.entity instanceof EndCrystalEntity)))) {
                    lastColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
                    event.doublePass = true;
                }
                event.outlineVertexConsumers.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            }
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (shaderWorks && outlineShader != null && (outline.getValue() || fill.getValue() || playerLines.getValue() || crystalLines.getValue() || otherLines.getValue())) {
            for (PostProcessShader postProcessShader : ((ShaderEffectAccessor) outlineShader).getPasses()) {
                postProcessShader.getProgram().getUniformByNameOrDummy("outlineAlpha").set(outline.getValue() ? 1F : 0F);
                postProcessShader.getProgram().getUniformByNameOrDummy("filledAlpha").set(fill.getValue() ? opacity.getValue() / 10F  : 0F);
                postProcessShader.getProgram().getUniformByNameOrDummy("width").set(width.getValue());
                postProcessShader.getProgram().getUniformByNameOrDummy("dotSpacing").set(dotSpacing.getValue());
                postProcessShader.getProgram().getUniformByNameOrDummy("vertical").set(vertical.getValue());
                postProcessShader.getProgram().getUniformByNameOrDummy("dotSize").set(dotSize.getValue());
            }
            mc.worldRenderer.loadEntityOutlineShader();
            shaderOutputFB.clear(mc.IS_SYSTEM_MAC);
            mc.getFramebuffer().beginWrite(false);
        }
    }

    @EventHandler
    public void onDrawVerteciesForShaders(DrawVerteciesForShadersEvent event) {
        if (shaderWorks && outlineShader != null && (outline.getValue() || fill.getValue() || playerLines.getValue() || crystalLines.getValue() || otherLines.getValue())) {
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
        if (shaderWorks && outlineShader != null && (outline.getValue() || fill.getValue() || playerLines.getValue() || crystalLines.getValue() || otherLines.getValue())) {
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
            outlineShader = new ShaderEffect(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), new Identifier("shaders/post/outline_shader.json"));
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
