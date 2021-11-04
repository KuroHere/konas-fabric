package com.konasclient.konas.module.modules.render;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.render.EntityRenderEvent;
import com.konasclient.konas.event.events.render.EntityScaleEvent;
import com.konasclient.konas.event.events.render.RenderEntityModelEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.ColorSetting;
import com.konasclient.konas.setting.Parent;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.friend.Friends;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

public class Chams extends Module {
    private final Setting<Parent> selection = new Setting<>("Selection", new Parent(false));
    private final Setting<Boolean> players = new Setting<>("Players", true).withParent(selection);
    private final Setting<Boolean> friends = new Setting<>("Friends", true).withParent(selection);
    private final Setting<Boolean> crystals = new Setting<>("Crystals", true).withParent(selection);
    private final Setting<Boolean> creatures = new Setting<>("Creatures", false).withParent(selection);
    private final Setting<Boolean> monsters = new Setting<>("Monsters", false).withParent(selection);
    private final Setting<Boolean> ambients = new Setting<>("Ambients", false).withParent(selection);

    private static final Setting<Parent> colors = new Setting<>("Colors", new Parent(false));
    public static final Setting<ColorSetting> hand = new Setting<>("Hand", new ColorSetting(0xFFFFFFFF)).withParent(colors);
    private final Setting<ColorSetting> player = new Setting<>("Player", new ColorSetting(0xFFFFFFFF)).withParent(colors);
    private final Setting<ColorSetting> friend = new Setting<>("Friend", new ColorSetting(0xFFFFFFFF)).withParent(colors);
    private final Setting<ColorSetting> crystal = new Setting<>("Crystal", new ColorSetting(0xFFFFFFFF)).withParent(colors);
    private final Setting<ColorSetting> creature = new Setting<>("Creature", new ColorSetting(0xFFFFFFFF)).withParent(colors);
    private final Setting<ColorSetting> monster = new Setting<>("Monster", new ColorSetting(0xFFFFFFFF)).withParent(colors);
    private final Setting<ColorSetting> ambient = new Setting<>("Ambient", new ColorSetting(0xFFFFFFFF)).withParent(colors);

    private static final Setting<Parent> textures = new Setting<>("Textures", new Parent(false));
    public static final Setting<Boolean> handTextures = new Setting<>("HandTextures", true).withParent(textures);
    private static final Setting<Boolean> playerTextures = new Setting<>("PlayerTextures", true).withParent(textures);
    private static final Setting<Boolean> friendTextures = new Setting<>("FriendTextures", true).withParent(textures);
    private static final Setting<Boolean> crystalTextures = new Setting<>("CrystalTextures", true).withParent(textures);
    private static final Setting<Boolean> creatureTextures = new Setting<>("CreatureTextures", true).withParent(textures);
    private static final Setting<Boolean> monsterTextures = new Setting<>("MonsterTextures", true).withParent(textures);
    private static final Setting<Boolean> ambientTextures = new Setting<>("AmbientTextures", true).withParent(textures);

    private final Setting<Parent> scales = new Setting<>("Scales", new Parent(false));
    private final Setting<Float> playerScale = new Setting<>("PlayerScale", 1F, 5F, -5F, 0.1F).withParent(scales);
    private final Setting<Float> friendScale = new Setting<>("FriendScale", 1F, 5F, -5F, 0.1F).withParent(scales);
    private final Setting<Float> crystalScale = new Setting<>("CrystalScale", 2F, 5F, -5F, 0.1F).withParent(scales);
    private final Setting<Float> creatureScale = new Setting<>("CreatureScale", 1F, 5F, -5F, 0.1F).withParent(scales);
    private final Setting<Float> monsterScale = new Setting<>("MonsterScale", 1F, 5F, -5F, 0.1F).withParent(scales);
    private final Setting<Float> ambientScale = new Setting<>("AmbientScale", 1F, 5F, -5F, 0.1F).withParent(scales);

    public Chams() {
        super("chams", "Modify how entities are rendered", 0xFFD48354, Category.Render);
    }

    public static final Identifier EMPTY_TEXTURE = new Identifier("textures/empty.png");

    @EventHandler
    public void onEntityRenderPre(EntityRenderEvent.Pre event) {
        if (shouldRender(event.entity)) {
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glPolygonOffset(1.0f, -1100000.0f);
        }
    }

    @EventHandler
    public void onEntityRenderPost(EntityRenderEvent.Post event) {
        if (shouldRender(event.entity)) {
            GL11.glPolygonOffset(1.0f, 1100000.0f);
            GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        }
    }

    @EventHandler
    public void onEntityScale(EntityScaleEvent event) {
        float scale = getEntityScale(event.entity);
        event.x = scale;
        event.y = event.entity instanceof EndCrystalEntity ? scale : -scale;
        event.z = scale;
    }

    @EventHandler
    public void onRenderEntityModel(RenderEntityModelEvent event) {
        ColorSetting clr = getEntityColor(event.entity);
        event.red = clr.getRed() / 255F;
        event.green = clr.getGreen() / 255F;
        event.blue = clr.getBlue() / 255F;
        event.alpha = clr.getAlpha() / 255F;
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

        switch (entity.getType().getSpawnGroup()) {
            case CREATURE:
            case WATER_AMBIENT:
            case WATER_CREATURE: return creature.getValue();
            case MONSTER: return monster.getValue();
            case AMBIENT: return ambient.getValue();
            default: return new ColorSetting(0xFFFFFFFF);
        }
    }

    private float getEntityScale(Entity entity) {
        if (entity instanceof PlayerEntity) {
            if (Friends.isFriend(entity.getName().asString())) {
                return friendScale.getValue();
            }

            return playerScale.getValue();
        }

        if (entity instanceof EndCrystalEntity) {
            return crystalScale.getValue();
        }

        switch (entity.getType().getSpawnGroup()) {
            case CREATURE:
            case WATER_AMBIENT:
            case WATER_CREATURE: return creatureScale.getValue();
            case MONSTER: return monsterScale.getValue();
            case AMBIENT: return ambientScale.getValue();
            default: return 1F;
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

        switch (entity.getType().getSpawnGroup()) {
            case CREATURE:
            case WATER_AMBIENT:
            case WATER_CREATURE: return creatures.getValue();
            case MONSTER: return monsters.getValue();
            case AMBIENT: return ambients.getValue();
            default: return false;
        }
    }

    public static boolean shouldRenderTexture(Entity entity) {
        if (entity == null) {
            return false;
        }

        if (entity instanceof PlayerEntity) {
            if (entity == MinecraftClient.getInstance().player) return false;

            if (Friends.isFriend(entity.getName().asString())) {
                return friendTextures.getValue();
            }

            return playerTextures.getValue();
        }

        if (entity instanceof EndCrystalEntity) {
            return crystalTextures.getValue();
        }

        switch (entity.getType().getSpawnGroup()) {
            case CREATURE:
            case WATER_AMBIENT:
            case WATER_CREATURE: return creatureTextures.getValue();
            case MONSTER: return monsterTextures.getValue();
            case AMBIENT: return ambientTextures.getValue();
            default: return false;
        }
    }
}
