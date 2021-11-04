package com.konasclient.konas.module.modules.client;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.render.PotionRenderHUDEvent;
import com.konasclient.konas.event.events.render.RenderHudEvent;
import com.konasclient.konas.event.events.world.UpdateEvent;
import com.konasclient.konas.mixin.MinecraftClientAccessor;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.combat.AutoCrystal;
import com.konasclient.konas.setting.ColorSetting;
import com.konasclient.konas.setting.Parent;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.StringUtils;
import com.konasclient.konas.util.client.TickRateUtil;
import com.konasclient.konas.util.client.Timer;
import com.konasclient.konas.util.entity.EntityUtils;
import com.konasclient.konas.util.entity.PlayerUtils;
import com.konasclient.konas.util.render.font.DefaultFontRenderer;
import com.konasclient.konas.util.render.font.FontRenderWrapper;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class HUD extends Module {
    public static int cps = 0;

    private Setting<ColorSetting> startColSetting = new Setting<>("StartColor", new ColorSetting(0xFFE86666, true));
    private Setting<ColorSetting> endColSetting = new Setting<>("EndColor", new ColorSetting(0xFFE86666, true, 5000));

    private Setting<Corner> corner = new Setting<>("Corner", Corner.Right);

    private Setting<Boolean> watermark = new Setting<>("Watermark", true);
    private Setting<Boolean> facing = new Setting<>("Facing", true);
    private Setting<Boolean> coords = new Setting<>("Coords", true);
    private Setting<Boolean> netherCoords = new Setting<>("NetherCoords", true);

    private Setting<Side> side = new Setting<>("Side", Side.Top);

    private Setting<Boolean> arrayList = new Setting<>("ArrayList", true);
    private Setting<ModuleColors> moduleColors = new Setting<>("Coloring", ModuleColors.Hud);
    private Setting<Sorting> sorting = new Setting<>("Sorting", Sorting.Normal);
    private Setting<Integer> arrayListFactor = new Setting<>("AnimSpeed", 4, 10, 1, 1);

    private Setting<Parent> misc = new Setting<>("Misc", new Parent(false));
    private Setting<Boolean> potions = new Setting<>("Potions", true).withParent(misc);
    private Setting<Boolean> colored = new Setting<>("Colored", true).withParent(misc);
    private Setting<Boolean> speed = new Setting<>("Speed", true).withParent(misc);
    private Setting<Boolean> ping = new Setting<>("Ping", true).withParent(misc);
    private Setting<Boolean> fps = new Setting<>("FPS", false).withParent(misc);
    private Setting<Boolean> tps = new Setting<>("TPS", true).withParent(misc);

    private Setting<Parent> items = new Setting<>("Items", new Parent(false));
    private Setting<Boolean> sets = new Setting<>("Sets", true).withParent(items);
    private Setting<Boolean> totems = new Setting<>("Tots", false).withParent(items);
    private Setting<Boolean> endCrystals = new Setting<>("ECs", false).withParent(items);
    private Setting<Boolean> experience = new Setting<>("EXp", false).withParent(items);
    private Setting<Boolean> goldenApples = new Setting<>("GApps", false).withParent(items);

    private enum Corner {
        Left, Right
    }

    private enum Side {
        Top,
        Bottom
    }

    private enum ModuleColors {
        Module, Hud
    }

    private enum Sorting {
        Normal, Alphabetical
    }

    public enum AnimationState {
        OPEN,
        CLOSE,
        NONE
    }

    public HUD() {
        super("HUD", "Heads Up Display", 0xFF42D96D, Category.Client);
        toggle(true);
    }

    private int width;
    private int height;

    public final ArrayList<ArrayListElement> arrayListElements = new ArrayList<>();
    public final Timer arrayTimer = new Timer();

    double blocksPerTick = 0.0d;
    double[] bptMap = new double[30];
    int offset = 0;

    @EventHandler
    public void onUpdatePre(UpdateEvent.Pre event) {
        if (arrayTimer.hasPassed(100) && arrayList.getValue()) {
            updateArrayList();
        }

        blocksPerTick = Math.sqrt(((mc.player.getX() - mc.player.prevX) * (mc.player.getX() - mc.player.prevX)) + ((mc.player.getZ() - mc.player.prevZ) * (mc.player.getZ() - mc.player.prevZ)));
        if(offset > 29) {
            offset = 0;
        }
        bptMap[offset] = blocksPerTick;
        offset++;
    }

    private double getSpeedKph() {
        double avgTotal = 0.0d;
        for(int i = 0; i < 30; i++) {
            avgTotal += bptMap[i];
        }
        double currentBPTAverage = avgTotal/30;
        return ((currentBPTAverage * 20) * 3.6);
    }

    private final Comparator<TextElement> sortByLength = (first, second) -> {
        final float dif = FontRenderWrapper.getStringWidth(second.getText()) - FontRenderWrapper.getStringWidth(first.getText());
        return dif != 0 ? (int) dif : second.getText().compareTo(first.getText());
    };

    private void drawMisc() {
        ArrayList<TextElement> pots = new ArrayList<>();
        ArrayList<TextElement> normal = new ArrayList<>();

        if (potions.getValue()) {
            Collection<StatusEffectInstance> effects = mc.player.getStatusEffects();
            if (effects != null && !effects.isEmpty()) {
                for (StatusEffectInstance effect : effects) {
                    if (effect != null) {
                        StatusEffect potion = effect.getEffectType();
                        if (potion != null) {
                            String name = I18n.translate(potion.getTranslationKey());
                            if (effect.getAmplifier() == 1) {
                                name+=" 2";
                            } else if (effect.getAmplifier() == 2) {
                                name+=" 3";
                            } else if (effect.getAmplifier() == 3) {
                                name+=" 4";
                            }
                            name += " " + getPotionDurationString(effect, 1.0F);
                            pots.add(new TextElement(name, potion.getColor(), colored.getValue()));
                            pots.sort(sortByLength);
                        }
                    }
                }
            }
        }

        if (speed.getValue()) {
            normal.add(new TextElement("Speed " + String.format("%,.2f", getSpeedKph()) + " km/h"));
        }

        if (ping.getValue()) {
            normal.add(new TextElement("Ping " + EntityUtils.getPing(mc.player) + " ms"));
        }

        if (fps.getValue()) {
            normal.add(new TextElement("FPS " + ((MinecraftClientAccessor) mc).getCurrentFps()));
        }

        if (tps.getValue()) {
            normal.add(new TextElement("TPS " + String.format("%,.2f", TickRateUtil.getTickRate())));
        }

        if (sets.getValue()) {
            normal.add(new TextElement("Sets " + PlayerUtils.countSets()));
        }

        if (totems.getValue()) {
            normal.add(new TextElement("Tots " + PlayerUtils.countItem(Items.TOTEM_OF_UNDYING)));
        }

        if (endCrystals.getValue()) {
            normal.add(new TextElement("ECs " + PlayerUtils.countItem(Items.END_CRYSTAL)));
        }

        if (experience.getValue()) {
            normal.add(new TextElement("EXp " + PlayerUtils.countItem(Items.EXPERIENCE_BOTTLE)));
        }

        if (goldenApples.getValue()) {
            normal.add(new TextElement("GApps " + PlayerUtils.countItem(Items.GOLDEN_APPLE)));
        }

        pots.sort(sortByLength);
        normal.sort(sortByLength);
        ArrayList<TextElement> text = new ArrayList<>();
        text.addAll(pots);
        text.addAll(normal);

        if(side.getValue() == Side.Top) {
            int textY = height - 2;
            for (TextElement s : text) {
                int thisX = corner.getValue() == Corner.Left ? 2 : (int) (width -(FontRenderWrapper.getStringWidth(s.getText())) - 2);
                textY -= FontRenderWrapper.getFontHeight();
                if(s.isPot()) {
                    FontRenderWrapper.drawStringWithShadow(s.getText(), thisX, textY, 0xff000000 | s.getColor());
                } else {
                    FontRenderWrapper.drawStringWithShadow(s.getText(), thisX, textY, colorAt(thisX, textY));
                }
            }
        } else {
            int textY = 2;
            for (TextElement s : text) {
                int thisX = corner.getValue() == Corner.Left ? 2 : (int) (width -(FontRenderWrapper.getStringWidth(s.getText())) - 2);
                if(s.isPot()) {
                    FontRenderWrapper.drawStringWithShadow(s.getText(), thisX, textY, 0xff000000 | s.getColor());
                } else {
                    FontRenderWrapper.drawStringWithShadow(s.getText(), thisX, textY, colorAt(thisX, textY));
                }
                textY += FontRenderWrapper.getFontHeight();
            }
        }
    }

    private String getPotionDurationString(StatusEffectInstance effect, float durationFactor) {
        if (effect.isPermanent()) {
            return "**:**";
        } else {
            int i = MathHelper.floor((float)effect.getDuration() * durationFactor);
            return StringUtils.ticksToElapsedTime(i);
        }
    }

    private void updateArrayList() {
        for(Module module : ModuleManager.getModules()) {
            if(module.isActive() && !module.getCategory().equals(Category.Client)) {
                if(!arrayListContainsModule(module)) {
                    ArrayListElement element = new ArrayListElement(module);
                    element.state = AnimationState.OPEN;
                    arrayListElements.add(element);
                }
            } else {
                if(arrayListContainsModule(module)) {
                    ArrayListElement element = getArrayListElement(module);
                    if(element != null) {
                        element.state = AnimationState.CLOSE;
                    }
                }
            }
            if(arrayListContainsModule(module) && module.getCategory().equals(Category.Client)) {
                arrayListElements.remove(getArrayListElement(module));
            }
        }
        sortModules();
    }

    private void sortModules() {
        Comparator<ArrayListElement> comparator = (first, second) ->
        {
            if(sorting.getValue() == Sorting.Normal) {
                String firstName = first.module.getTitle() + (first.module.getMetadata().length() > 0 ? " [" + first.module.getMetadata() + "]" : "");
                String secondName = second.module.getTitle() + (second.module.getMetadata().length() > 0 ? " [" + second.module.getMetadata() + "]" : "");
                float dif = FontRenderWrapper.getStringWidth(secondName) - FontRenderWrapper.getStringWidth(firstName);
                return dif != 0 ? (int) dif : secondName.compareTo(firstName);
            } else {
                String firstName = first.module.getTitle() + (first.module.getMetadata().length() > 0 ? " [" + first.module.getMetadata() + "]" : "");
                String secondName = second.module.getTitle() + (second.module.getMetadata().length() > 0 ? " [" + second.module.getMetadata() + "]" : "");
                return secondName.compareTo(firstName);
            }
        };
        arrayListElements.sort(comparator);
    }

    private boolean arrayListContainsModule(Module module) {
        for(ArrayListElement arrayListElement : arrayListElements) {
            if(arrayListElement.module == module) {
                return true;
            }
        }
        return false;
    }

    private ArrayListElement getArrayListElement(Module module) {
        for(ArrayListElement arrayListElement : arrayListElements) {
            if(arrayListElement.module == module) {
                return arrayListElement;
            }
        }
        return null;
    }

    @EventHandler
    public void onPotionRenderHUD(PotionRenderHUDEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onRenderHud(RenderHudEvent event) {
        width = mc.getWindow().getScaledWidth();
        height = mc.getWindow().getScaledHeight();

        if (watermark.getValue()) drawWatermark();

        if (coords.getValue()) drawCoords();

        if (arrayList.getValue()) drawArrayList();

        drawMisc();

        if (AutoCrystal.debug.getValue() && ModuleManager.get(AutoCrystal.class).isActive()) {
            float cY = height / 4F;
            FontRenderWrapper.drawStringWithShadow("CPS: " + AutoCrystal.getCPS(), 2, cY, colorAt(2, cY));
        }
    }

    private void drawCoords() {
        int offsetY = mc.currentScreen instanceof ChatScreen ? DefaultFontRenderer.INSTANCE.getFontHeight() + 6 : 2;
        if (coords.getValue()) {
            String xC = String.format("%.1f", mc.player.getX());
            String yC = String.format("%.1f", mc.player.getY());
            String zC = String.format("%.1f", mc.player.getZ());

            String coordsText = "XYZ " + xC + ", " + yC + ", " + zC;
            if(netherCoords.getValue()) {
                String netherX = !isNether() ? String.format("%.1f", mc.player.getX() / 8) : String.format("%.1f", mc.player.getX() * 8);
                String netherZ = !isNether() ? String.format("%.1f", mc.player.getZ() / 8) : String.format("%.1f", mc.player.getZ() * 8);
                coordsText += " [" + netherX + ", " + netherZ + "]";
            }
            int x = corner.getValue() == Corner.Left ? (int) (width - FontRenderWrapper.getStringWidth(coordsText) - 1.5D) : 2;
            offsetY += FontRenderWrapper.getStringHeight(coordsText);
            FontRenderWrapper.drawStringWithShadow(coordsText, x, height - offsetY, colorAt(x, height - offsetY));
        }
        if (facing.getValue()) {
            String facingText = getFacing() + " [" + getCompassDirection() + "]";
            int x = corner.getValue() == Corner.Left ? (int) (width - FontRenderWrapper.getStringWidth(facingText) - 1.5D) : 2;
            offsetY += FontRenderWrapper.getStringHeight(facingText);
            FontRenderWrapper.drawStringWithShadow(facingText, x, height - offsetY, colorAt(x, height - offsetY));
        }
    }

    private boolean isNether() {
        return mc.world.getRegistryKey().getValue().getPath().equals("the_nether");
    }

    private String getFacing() {
        String directionLabel = mc.player.getHorizontalFacing().getName().toLowerCase();
        switch (directionLabel) {
            case "north":
                directionLabel = "North";
                break;

            case "south":
                directionLabel = "South";
                break;

            case "west":
                directionLabel = "West";
                break;

            case "east":
                directionLabel = "East";
                break;
        }
        return directionLabel;
    }

    private String getCompassDirection() {
        String directionLabel = "";
        switch (mc.player.getHorizontalFacing().getName().toLowerCase()) {
            case "north":
                directionLabel = "-Z";
                break;
            case "south":
                directionLabel = "+Z";
                break;
            case "west":
                directionLabel = "-X";
                break;

            case "east":
                directionLabel = "+X";
                break;
        }
        return directionLabel;
    }

    private void drawWatermark() {
        int x = corner.getValue() == Corner.Left ? (int) (width - FontRenderWrapper.getStringWidth(Konas.NAME + " " + Konas.VERSION) - 1.5) : 2;
        FontRenderWrapper.drawStringWithShadow(Konas.NAME + " ", x, 2, colorAt(x, 2));
        int versionX = x + (int) (FontRenderWrapper.getStringWidth(Konas.NAME + " ") + 0.5);
        FontRenderWrapper.drawStringWithShadow(Konas.VERSION, versionX, 2, colorAt(versionX, 2, true));
    }

    private void drawArrayList() {
        if(arrayList.getValue()) {
            if(side.getValue() == Side.Top) {
                int textY = 2;
                if(sorting.getValue() == Sorting.Normal) {
                    for (ArrayListElement element : arrayListElements) {
                        String moduleInfoString = element.module.getMetadata().isEmpty() ? "" : " [" + element.module.getMetadata() + "]";
                        String renderString = element.module.getTitle();
                        int currentX = corner.getValue() == Corner.Left ? 2 : (int) (width - FontRenderWrapper.getStringWidth(renderString) - FontRenderWrapper.getStringWidth(moduleInfoString)) - 2;

                        if (element.state == AnimationState.OPEN) {
                            element.ticks = element.ticks - arrayListFactor.getValue();
                            if (element.ticks <= 0) {
                                element.state = AnimationState.NONE;
                            } else {
                                currentX =  corner.getValue() == Corner.Left ? 2 - element.ticks : (int) (width - FontRenderWrapper.getStringWidth(renderString) - FontRenderWrapper.getStringWidth(moduleInfoString)) - 2 + element.ticks;
                            }
                        } else if (element.state == AnimationState.CLOSE) {
                            if (element.ticks <= 0) {
                                element.ticks = 1;
                            }
                            element.ticks += arrayListFactor.getValue();
                            currentX =  corner.getValue() == Corner.Left ? 2 - element.ticks : (int) (width - FontRenderWrapper.getStringWidth(renderString) - FontRenderWrapper.getStringWidth(moduleInfoString)) - 2 + element.ticks;
                        }
                        FontRenderWrapper.drawStringWithShadow(renderString, currentX, textY, moduleColors.getValue() == ModuleColors.Module ? element.module.getColor() : colorAt(currentX, textY));
                        FontRenderWrapper.drawStringWithShadow(moduleInfoString, currentX + FontRenderWrapper.getStringWidth(renderString + " ") - 3, textY, moduleColors.getValue() == ModuleColors.Module ? new Color(element.module.getColor()).darker().getRGB() : colorAt(currentX, textY, true));
                        textY += FontRenderWrapper.getFontHeight();
                    }
                } else {
                    for (int i = arrayListElements.size() - 1; i >= 0; i--) {
                        String moduleInfoString = arrayListElements.get(i).module.getMetadata().isEmpty() ? "" : " [" + arrayListElements.get(i).module.getMetadata() + "]";
                        String renderString = arrayListElements.get(i).module.getTitle();
                        int currentX = corner.getValue() == Corner.Left ? 2 : (int) (width - FontRenderWrapper.getStringWidth(renderString) - FontRenderWrapper.getStringWidth(moduleInfoString)) - 2;

                        if (arrayListElements.get(i).state == AnimationState.OPEN) {
                            arrayListElements.get(i).ticks = arrayListElements.get(i).ticks - arrayListFactor.getValue();
                            if (arrayListElements.get(i).ticks <= 0) {
                                arrayListElements.get(i).state = AnimationState.NONE;
                            } else {
                                currentX = corner.getValue() == Corner.Left ? 2 - arrayListElements.get(i).ticks : (int) (width - FontRenderWrapper.getStringWidth(renderString) - FontRenderWrapper.getStringWidth(moduleInfoString)) - 2 + arrayListElements.get(i).ticks;
                            }
                        } else if (arrayListElements.get(i).state == AnimationState.CLOSE) {
                            if (arrayListElements.get(i).ticks <= 0) {
                                arrayListElements.get(i).ticks = 1;
                            }
                            arrayListElements.get(i).ticks += arrayListFactor.getValue();
                            currentX = corner.getValue() == Corner.Left ? 2 - arrayListElements.get(i).ticks : (int) (width - FontRenderWrapper.getStringWidth(renderString) - FontRenderWrapper.getStringWidth(moduleInfoString)) - 2 + arrayListElements.get(i).ticks;
                        }
                        FontRenderWrapper.drawStringWithShadow(renderString, currentX, textY, moduleColors.getValue() == ModuleColors.Module ? arrayListElements.get(i).module.getColor() : colorAt(currentX, textY));
                        FontRenderWrapper.drawStringWithShadow(moduleInfoString, currentX + FontRenderWrapper.getStringWidth(renderString + " ") - 3, textY, moduleColors.getValue() == ModuleColors.Module ? new Color(arrayListElements.get(i).module.getColor()).darker().getRGB() : colorAt(currentX, textY, true));
                        textY += FontRenderWrapper.getFontHeight();
                    }
                }
                arrayListElements.removeIf(element -> element.state == AnimationState.CLOSE && element.ticks >= 50);
            } else {
                int textY = height-(arrayListElements.size()*10)-3;
                if(mc.currentScreen instanceof ChatScreen) textY = textY-15;
                for (int i = arrayListElements.size() - 1; i >= 0; i--) {
                    if(arrayListElements.get(i) == null) {
                        return;
                    }
                    String moduleInfoString = arrayListElements.get(i).module.getMetadata().isEmpty() ? "" : "[" + arrayListElements.get(i).module.getMetadata() + "]";
                    String renderString = arrayListElements.get(i).module.getTitle();
                    int currentX = corner.getValue() == Corner.Left ? 2 : (int) (width - FontRenderWrapper.getStringWidth(renderString) - FontRenderWrapper.getStringWidth(moduleInfoString)) - 2;

                    if(arrayListElements.get(i).state == AnimationState.OPEN) {
                        arrayListElements.get(i).ticks = arrayListElements.get(i).ticks-arrayListFactor.getValue();
                        if(arrayListElements.get(i).ticks <= 0) {
                            arrayListElements.get(i).state = AnimationState.NONE;
                        } else {
                            currentX = corner.getValue() == Corner.Left ? 2 - arrayListElements.get(i).ticks : (int) (width - FontRenderWrapper.getStringWidth(renderString) - FontRenderWrapper.getStringWidth(moduleInfoString)) - 2 + arrayListElements.get(i).ticks;
                        }
                    } else if(arrayListElements.get(i).state == AnimationState.CLOSE) {
                        if(arrayListElements.get(i).ticks <= 0) {
                            arrayListElements.get(i).ticks = 1;
                        }
                        arrayListElements.get(i).ticks+=arrayListFactor.getValue();
                        currentX = corner.getValue() == Corner.Left ? 2 - arrayListElements.get(i).ticks : (int) (width - FontRenderWrapper.getStringWidth(renderString) - FontRenderWrapper.getStringWidth(moduleInfoString)) - 2 + arrayListElements.get(i).ticks;
                    }
                    FontRenderWrapper.drawStringWithShadow(renderString, currentX, textY, moduleColors.getValue() == ModuleColors.Module ? arrayListElements.get(i).module.getColor() : colorAt(currentX, textY));
                    FontRenderWrapper.drawStringWithShadow(moduleInfoString, currentX + (int) FontRenderWrapper.getStringWidth(renderString + " ") - 3, textY, moduleColors.getValue() == ModuleColors.Module ? new Color(arrayListElements.get(i).module.getColor()).darker().getRGB() : colorAt(currentX, textY, true));
                    textY += FontRenderWrapper.getFontHeight();
                }
                arrayListElements.removeIf(element-> element.state == AnimationState.CLOSE && element.ticks >= 50);
            }
        }
    }

    private int colorAt(float x, float y) {
        return colorAt(x, y, false);
    }

    private int colorAt(float x, float y, boolean darker) {
        Color startCol = startColSetting.getValue().getColorObject();
        Color endCol = endColSetting.getValue().getColorObject();

        float dx1 = width;
        float dy1 = height;

        float dx2 = -dy1;
        float dy2 = dx1;
        float denom = (dy2 * dx1) - (dx2 * dy1);

        if (denom == 0) {
            return 0;
        }

        float ua = (dx2 * (-y)) - (dy2 * (-x));
        ua /= denom;
        float ub = (dx1 * (-y)) - (dy1 * (-x));
        ub /= denom;
        float u = ua;
        if (u < 0) {
            u = 0;
        }
        if (u > 1) {
            u = 1;
        }
        float v = 1 - u;

        // u is the proportion down the line we are
        int r = (int) ((u * endCol.getRed()) + (v * startCol.getRed()));
        int b = (int) ((u * endCol.getBlue()) + (v * startCol.getBlue()));
        int g = (int) ((u * endCol.getGreen()) + (v * startCol.getGreen()));
        int a = (int) ((u * endCol.getAlpha()) + (v * startCol.getAlpha()));

        if (darker) {
            return new Color(r, g, b, a).darker().getRGB();
        } else {
            return new Color(r, g, b, a).getRGB();
        }
    }

    private class ArrayListElement {
        public Module module;
        public AnimationState state;
        public int ticks;

        public ArrayListElement(Module module) {
            this.module = module;
            this.ticks = 50;
            this.state = AnimationState.NONE;
        }
    }

    private class TextElement {
        private final boolean isPotion;
        private final String text;
        private final int color;

        public TextElement(String text) {
            this.text = text;
            this.color = -1;
            this.isPotion = false;
        }

        public TextElement(String text, int color, boolean ispot) {
            this.text = text;
            this.color = color;
            this.isPotion = ispot;
        }

        public String getText() {
            return this.text;
        }

        public int getColor() {
            return this.color;
        }

        public boolean isPot() {
            return this.isPotion;
        }
    }
}
