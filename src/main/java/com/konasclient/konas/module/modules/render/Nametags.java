package com.konasclient.konas.module.modules.render;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.render.RenderHudEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.module.modules.client.FontModule;
import com.konasclient.konas.setting.ColorSetting;
import com.konasclient.konas.setting.ListenableSettingDecorator;
import com.konasclient.konas.setting.Parent;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.entity.EntityUtils;
import com.konasclient.konas.util.math.*;
import com.konasclient.konas.util.render.font.CustomFontRenderer;
import com.konasclient.konas.util.render.font.FontManager;
import com.konasclient.konas.util.render.mesh.DrawMode;
import com.konasclient.konas.util.render.mesh.MeshBuilder;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.atomic.AtomicReference;

import static org.lwjgl.opengl.GL11.*;


public class Nametags extends Module {
    public static final Setting<Parent> name = new Setting<>("Name", new Parent(false));
    private static Setting<Boolean> gamemode = new Setting<>("Gamemode", false).withParent(name);
    private static Setting<Boolean> ping = new Setting<>("Ping", false).withParent(name);
    private static Setting<Boolean> health = new Setting<>("Health", true).withParent(name);
    private static Setting<Boolean> pops = new Setting<>("Pops", false).withParent(name);
    private static Setting<Boolean> fill = new Setting<>("Fill", true).withParent(name);
    private static Setting<Boolean> outline = new Setting<>("Outline", true).withParent(name);
    private static Setting<Integer> margin = new Setting<>("Margin", 2, 10, 0, 1).withParent(name);
    private static Setting<Double> spacing = new Setting<>("Spacing", 0.3D, 1D, 0D, 0.05D).withParent(name);
    private static Setting<Boolean> armor = new Setting<>("Armor", true).withParent(name);
    private static Setting<Integer> gap = new Setting<>("Gap", 5, 20, 0, 1).withParent(name);
    private static Setting<Integer> armorSpacing = new Setting<>("ArmorSpacing", 35, 100, 10, 1).withParent(name);
    private static Setting<Boolean> enchants = new Setting<>("Enchants", true).withParent(name);
    private static Setting<Boolean> mainhandName = new Setting<>("Mainhand", true).withParent(name);

    private final Setting<Parent> selection = new Setting<>("Selection", new Parent(false));
    private final Setting<Boolean> players = new Setting<>("Players", true).withParent(selection);
    private final Setting<Boolean> self = new Setting<>("Self", false).withVisibility(players::getValue).withParent(selection);
    private final Setting<Boolean> shulkers = new Setting<>("Shulkers", false).withParent(selection);
    private final Setting<Boolean> waypoints = new Setting<>("Waypoints", true).withParent(selection);

    private static final Setting<Parent> scaling = new Setting<>("Scaling", new Parent(false));
    public static final Setting<Integer> fontSize = new ListenableSettingDecorator<>("FontSize", 34, 100, 20, 1, (value) -> FontManager.initFonts(FontModule.currentFont)).withParent(scaling);
    public static final Setting<Integer> enchantFontSize = new ListenableSettingDecorator<>("EnchantFontSize", 12, 20, 5, 1, (value) -> FontManager.initFonts(FontModule.currentFont)).withParent(scaling);
    private final Setting<Double> scaleFactor = new Setting<>("Factor", 0.6D, 1D, 0.01D, 0.01D).withParent(scaling);
    private final Setting<Double> maxScale = new Setting<>("ScaleLimit", 0.3D, 1D, 0.1D, 0.05D).withParent(scaling);

    public static final Setting<Parent> colors = new Setting<>("Colors", new Parent(false));
    private static Setting<ColorSetting> fillColorA = new Setting<>("FillColorA", new ColorSetting(0x80000000)).withParent(colors);
    private static Setting<ColorSetting> fillColorB = new Setting<>("FillColorB", new ColorSetting(0x80000000)).withParent(colors);
    private static Setting<ColorSetting> fillColorC = new Setting<>("FillColorC", new ColorSetting(0x80000000)).withParent(colors);
    private static Setting<ColorSetting> fillColorD = new Setting<>("FillColorD", new ColorSetting(0x80000000)).withParent(colors);
    private static Setting<ColorSetting> outlineColor = new Setting<>("OutlineColor", new ColorSetting(0xD0000000)).withParent(colors);

    public static CustomFontRenderer customFontRenderer;
    public static CustomFontRenderer enchantFontRenderer;

    public Nametags() {
        super("nametags", "Draws custom nametags above players", 0xFFCA4E6A, Category.Render);
    }

    private float spaceWidth = 0F;

    private MeshBuilder triangles = new MeshBuilder();

    @EventHandler
    public void onRenderHud(RenderHudEvent event) {
        if (FontManager.firstRun) {
            FontManager.firstRun = false;
            FontManager.initFonts(FontModule.currentFont);
        }

        Vec3d camPos = mc.gameRenderer.getCamera().getPos();

        spaceWidth = customFontRenderer.getStringWidth(" ");;

        for (PlayerEntity entity : mc.world.getPlayers()) {
            if (entity == mc.player && !self.getValue()) continue;
            if (Konas.currentFrustum != null) {
                if (!Konas.currentFrustum.isVisible(entity.getBoundingBox())) {
                    continue;
                }
            }
            Vec3d iPos = InterpolationUtil.lerpEntity(entity);
            BillboardPos projectedPos = MatrixUtil.getBillboardPos(iPos.add(0, entity.getHeight() + spacing.getValue(), 0));
            if (projectedPos == null) continue;
            Vec2d screenPos = projectedPos.getProjectedPos();
            if (screenPos == null) continue;


            drawNametag(entity, camPos, iPos, screenPos);

        }

    }

    private void drawNametag(PlayerEntity player, Vec3d camPos, Vec3d iPos, Vec2d screenPos)  {
        double height = 0;

        double dist = 1 - iPos.distanceTo(camPos) * (0.1D * scaleFactor.getValue());

        if (dist < maxScale.getValue()) {
            dist = maxScale.getValue();
        }

        glPushMatrix();
        glTranslated(screenPos.x, screenPos.y, 0);
        glScaled(dist, dist, 1D);

        ColoredString[] strings = new ColoredString[5];

        if (gamemode.getValue()) {
            strings[0] = new ColoredString("[" + (player.isCreative() ? "C" : "S") + "]", 0xFFFFFFFF);
        }

        if (ping.getValue()) {
            int ping = EntityUtils.getPing(player);
            if (ping > 0) {
                strings[1] = new ColoredString(Integer.toString(ping), ping > 150 ? 0xFFFF4848 : 0xFFFFFFFF);
            }
        }

        strings[2] = new ColoredString(player.getEntityName(), Targets.getTargetFontColor(player.getEntityName()));

        if (health.getValue()) {
            float playerHealth = RoundingUtil.roundFloat(player.getHealth() + player.getAbsorptionAmount(), 1);

            if (player.getEntityName().equalsIgnoreCase("antiflame") || player.getEntityName().equalsIgnoreCase("0851_")) {
                playerHealth += 0.69F;
            }

            String health = Float.toString(playerHealth).replace(".0", "");

            if (playerHealth < 5) {
                strings[3] = new ColoredString(health, 0xFFFF4848);
            } else if (playerHealth < 20) {
                strings[3] = new ColoredString(health, 0xFFFFCE48);
            } else {
                strings[3] = new ColoredString(health, 0xFF27CC00);
            }
        }

        if (pops.getValue()) {
            if (Targets.popList.containsKey(player.getEntityName())) {
                strings[4] = new ColoredString(Integer.toString(Targets.popList.get(player.getEntityName())), 0xFFFFFFFF);
            }
        }

        String total = "";
        boolean first = true;
        for (ColoredString part : strings) {
            if (part == null) continue;
            if (!first) {
                total += " ";
            }
            first = false;
            total += part.string;
        }

        float widthOffst = (customFontRenderer.getStringWidth(total) / 2F) + 2;

        triangles.begin(null, DrawMode.Triangles, VertexFormats.POSITION_COLOR);
        if (fill.getValue()) {
            triangles.quad(-(widthOffst + margin.getValue()), -(customFontRenderer.getFontHeight() + margin.getValue() * 2),(margin.getValue() + widthOffst) * 2,  customFontRenderer.getFontHeight() + margin.getValue() * 2, fillColorA.getValue().getRenderColor(), fillColorB.getValue().getRenderColor(), fillColorC.getValue().getRenderColor(), fillColorD.getValue().getRenderColor());
        }

        if (outline.getValue()) {
            triangles.quad(-(widthOffst + margin.getValue()), -(customFontRenderer.getFontHeight() + margin.getValue() * 2),(margin.getValue() + widthOffst) * 2,  1, outlineColor.getValue().getRenderColor(), outlineColor.getValue().getRenderColor(), outlineColor.getValue().getRenderColor(), outlineColor.getValue().getRenderColor());
            triangles.quad(-(widthOffst + margin.getValue()), -1,(margin.getValue() + widthOffst) * 2,  1, outlineColor.getValue().getRenderColor(), outlineColor.getValue().getRenderColor(), outlineColor.getValue().getRenderColor(), outlineColor.getValue().getRenderColor());
            triangles.quad(-(widthOffst + margin.getValue()), -(customFontRenderer.getFontHeight() + margin.getValue() * 2),1,  customFontRenderer.getFontHeight() + margin.getValue() * 2, outlineColor.getValue().getRenderColor(), outlineColor.getValue().getRenderColor(), outlineColor.getValue().getRenderColor(), outlineColor.getValue().getRenderColor());
            triangles.quad(widthOffst + margin.getValue() - 1, -(customFontRenderer.getFontHeight() + margin.getValue() * 2),1,  customFontRenderer.getFontHeight() + margin.getValue() * 2, outlineColor.getValue().getRenderColor(), outlineColor.getValue().getRenderColor(), outlineColor.getValue().getRenderColor(), outlineColor.getValue().getRenderColor());
        }
        triangles.end();

        widthOffst = -(widthOffst - 3);

        first = true;
        for (ColoredString part : strings) {
            if (part == null) continue;
            if (!first) {
                widthOffst += spaceWidth;
            }
            first = false;
            customFontRenderer.drawString(part.string, widthOffst, -(customFontRenderer.getFontHeight() + margin.getValue()), part.color);
            widthOffst += customFontRenderer.getStringWidth(part.string);
        }

        height += customFontRenderer.getFontHeight() + margin.getValue() * 2;

        if (enchants.getValue() || armor.getValue()) {
            height += gap.getValue();

            double xOffset = 0;

            for (ItemStack stack : mc.player.inventory.armor) {
                if (stack != null && !stack.isEmpty()) {
                    xOffset -= armorSpacing.getValue() / 2D;
                }
            }

            if (mc.player.getMainHandStack() != null && !mc.player.getMainHandStack().isEmpty()) {
                xOffset -= armorSpacing.getValue() / 2D;
            }

            if (mc.player.getOffHandStack() != null && !mc.player.getOffHandStack().isEmpty()) {
                xOffset -= armorSpacing.getValue() / 2D;
            }

            float maxOffset = 0F;

            if (mc.player.getMainHandStack() != null && !mc.player.getMainHandStack().isEmpty()) {
                maxOffset = Math.max(maxOffset, renderItem(mc.player.getMainHandStack(), xOffset, -height));
                xOffset += armorSpacing.getValue();
            }

            for (ItemStack stack : mc.player.inventory.armor) {
                if (stack != null && !stack.isEmpty()) {
                    maxOffset = Math.max(maxOffset, renderItem(stack, xOffset, -height));
                    xOffset += armorSpacing.getValue();
                }
            }

            if (mc.player.getOffHandStack() != null && !mc.player.getOffHandStack().isEmpty()) {
                maxOffset = Math.max(maxOffset, renderItem(mc.player.getOffHandStack(), xOffset, -height));
            }

            if (mainhandName.getValue() && mc.player.getMainHandStack() != null && !mc.player.getMainHandStack().isEmpty()) {
                maxOffset = Math.max(29, maxOffset) + 2;
                enchantFontRenderer.drawCenteredString(mc.player.getMainHandStack().getName().getString(), 0, (float) -(maxOffset + height), -1);
            }
        }

        glPopMatrix();
    }

    private float renderItem(ItemStack stack, double xPosition, double yPosition) {
        if (!stack.isEmpty()) {
            AtomicReference<Float> yOffset = new AtomicReference<>((float) 0);

            if (armor.getValue()) {
                glPushMatrix();
                glTranslated(xPosition, yPosition - 29, 0);
                glScaled(2, 2, 1);

                mc.getItemRenderer().renderGuiItemIcon(stack, 0, 0);
                mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, stack, 0, 0);

                glPopMatrix();
            }

            if (enchants.getValue()) {
                glTranslated(xPosition + 2D, yPosition, 0);
                EnchantmentHelper.get(stack).forEach((enc, level) -> {
                    try {
                        String encName = enc.isCursed()
                                ? enc.getName(0).getString().substring(9).substring(0, 1).toLowerCase()
                                : enc.getName(0).getString().substring(0, 1).toLowerCase();
                        encName = encName + level;

                        yOffset.set(yOffset.get() + enchantFontRenderer.getStringHeight(encName));

                        enchantFontRenderer.drawStringWithShadow(encName, 0, -yOffset.get(), -1);
                    } catch (IndexOutOfBoundsException exception) {

                    }

                });

                glTranslated(-(xPosition + 2D), -(yPosition), 0);
            }
            return yOffset.get();
        }
        return 0F;
    }

    private static class ColoredString {
        public final String string;
        public final int color;

        public ColoredString(String string, int color) {
            this.string = string;
            this.color = color;
        }
    }
}
