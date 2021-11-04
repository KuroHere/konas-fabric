package com.konasclient.konas.module.modules.combat;

import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Parent;
import com.konasclient.konas.setting.Setting;
import net.minecraft.entity.Entity;

public class Aura extends Module {
    private final Setting<Boolean> render = new Setting<>("Render", true);

    private final Setting<TimingMode> timing = new Setting<>("Timing", TimingMode.Sequential);

    private final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private final Setting<Boolean> strict = new Setting<>("Strict", true);

    private final Setting<Boolean> hitDelay = new Setting<>("HitDelay", true);
    private final Setting<Float> attackSpeed = new Setting<>("AttackSpeed", 10F, 20F, 1F, 0.5F).withVisibility(() -> !hitDelay.getValue());
    private final Setting<Boolean> autoBlock = new Setting<>("AutoBlock", true);

    private final Setting<Boolean> onlySword = new Setting<>("OnlySword", true);
    private final Setting<Boolean> autoSword = new Setting<>("AutoSword", true);

    private final Setting<Float> range = new Setting<>("Range", 4.2F, 6F, 1F, 0.1F);
    private final Setting<Float> wallsRange = new Setting<>("WallsRange", 1F, 6F, 1F, 0.1F);

    private final Setting<PriorityMode> priority = new Setting<>("Priority", PriorityMode.Smart);
    private final Setting<Boolean> multiTarget = new Setting<>("MultiTarget", true);
    private final Setting<Integer> ticksExisted = new Setting<>("TicksExisted", 50, 200, 0, 10);

    private final Setting<Spot> spot = new Setting<>("Spot", Spot.Nearest);

    private final Setting<Parent> targets = new Setting<>("Targets", new Parent(false));
    private final Setting<Boolean> nakeds = new Setting<>("Nakeds", false).withParent(targets);
    private final Setting<Boolean> players = new Setting<>("Players", true).withParent(targets);
    private final Setting<Boolean> friends = new Setting<>("Friends", false).withParent(targets);
    private final Setting<Boolean> creatures = new Setting<>("Creatures", false).withParent(targets);
    private final Setting<Boolean> monsters = new Setting<>("Monsters", false).withParent(targets);
    private final Setting<Boolean> ambients = new Setting<>("Ambients", false).withParent(targets);

    public static Setting<Parent> pause = new Setting<>("Pause", new Parent(false));
    public static Setting<Boolean> noMineSwitch = new Setting<>("Mining", false).withParent(pause);
    public static Setting<Boolean> noGapSwitch = new Setting<>("Gapping", false).withParent(pause);
    public static Setting<Boolean> rightClickGap = new Setting<>("RightClickGap", false).withVisibility(noGapSwitch::getValue).withParent(pause);
    public static Setting<Boolean> disableWhenAC = new Setting<>("AutoCrystal", false).withParent(pause);
    public static Setting<Float> disableUnderHealth = new Setting<>("Health", 2f, 10f, 0f, 0.5f).withParent(pause);

    private enum TimingMode {
        Sequential, Vanilla
    }

    private enum PriorityMode {
        Smart, Distance, Health
    }

    public enum Spot {
        Nearest,
        Chest,
        Feet,
        Head,
        Legs,
        Pelvis,
        Neck,
    }

    public Aura() {
        super("aura", "Attack entities", 0xFFE73535, Category.Combat);
    }

    public Entity getTarget() {
        return null;
    }
}
