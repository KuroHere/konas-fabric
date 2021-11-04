package com.konasclient.konas.module;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.client.ModuleInitialisationEvent;
import com.konasclient.konas.module.modules.client.*;
import com.konasclient.konas.module.modules.combat.*;
import com.konasclient.konas.module.modules.exploit.*;
import com.konasclient.konas.module.modules.misc.*;
import com.konasclient.konas.module.modules.misc.AutoEat;
import com.konasclient.konas.module.modules.movement.*;
import com.konasclient.konas.module.modules.player.*;
import com.konasclient.konas.module.modules.render.*;

import java.util.ArrayList;

public class ModuleManager {

    public static final int MESSAGE_ID = 1337;
    private static final ArrayList<Module> modules = new ArrayList<>();

    public static void init() {
        Konas.EVENT_BUS.post(ModuleInitialisationEvent.Pre.get());

        initCombat();
        initMovement();
        initPlayer();
        initRender();
        initMisc();
        initExploit();
        initClient();

        modules.sort((m1, m2) -> m1.getTitle().compareToIgnoreCase(m2.getTitle()));

        Konas.EVENT_BUS.post(ModuleInitialisationEvent.Post.get());
    }

    public static ArrayList<Module> getModules() {
        return modules;
    }

    public static ArrayList<String> getModulesAndAliases() {
        ArrayList<String> names = new ArrayList<>();

        for (Module module : modules) {
            names.add(module.getName());
            names.addAll(module.getAliases());
        }

        return names;
    }

    public static Module get(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) return module;

            for (String alias : module.getAliases()) {
                if (alias.equalsIgnoreCase(name)) return module;
            }
        }
        return null;
    }

    public static Module get(Class<? extends Module> clazz) {
        for (Module module : modules) {
            if (module.getClass() == clazz) return module;
        }
        return null;
    }

    public static ArrayList<Module> getActive() {
        ArrayList<Module> enabledModules = new ArrayList<>();
        modules.forEach(module -> {
            if (module.isActive()) enabledModules.add(module);
        });

        return enabledModules;
    }

    public static ArrayList<Module> getFromCategory(Category category) {
        ArrayList<Module> modulesInCategory = new ArrayList<>();
        modules.forEach(module -> {
            if (module.getCategory() == category) modulesInCategory.add(module);
        });

        return modulesInCategory;
    }

    private static void initCombat() {
        modules.add(new AntiSurround());
        modules.add(new Surround());
        modules.add(new Aura());
        modules.add(new AutoCrystal());
        modules.add(new Criticals());
        modules.add(new Offhand());
        modules.add(new AutoLog());
        modules.add(new AutoTrap());
        modules.add(new HoleFill());
        modules.add(new SelfBow());
        modules.add(new AutoWeb());
    }

    private static void initMovement() {
        modules.add(new IceSpeed());
        modules.add(new Speed());
        modules.add(new Sprint());
        modules.add(new SafeWalk());
    }

    private static void initPlayer() {
        modules.add(new AntiAim());
        modules.add(new AntiHunger());
        modules.add(new AntiLevitation());
        modules.add(new NoRotate());
        modules.add(new NoSlow());
        modules.add(new Reach());
        modules.add(new Step());
        modules.add(new Timer());
        modules.add(new Velocity());
        modules.add(new Swing());
        modules.add(new FakePlayer());
        modules.add(new ChestStealer());
        modules.add(new SkinBlinker());
        modules.add(new AutoEat());
    }

    private static void initRender() {
        modules.add(new BlockHighlight());
        modules.add(new FullBright());
        modules.add(new Interactions());
        modules.add(new NoRender());
        modules.add(new ESP());
        modules.add(new Breadcrumbs());
        modules.add(new CameraClip());
        modules.add(new Chams());
        modules.add(new Tooltips());
        modules.add(new Search());
        modules.add(new WorldOutline());
        modules.add(new Trails());
        modules.add(new OldAnimations());
        modules.add(new Nametags());
    }

    private static void initMisc() {
        modules.add(new AutoMount());
        modules.add(new AntiSpam());
        modules.add(new Scaffold());
    }

    private static void initExploit() {
        modules.add(new XCarry());
        modules.add(new SelfFill());
        modules.add(new AutoDupe());
        modules.add(new EntityControl());
        modules.add(new AirPlace());
        modules.add(new Blink());
    }

    private static void initClient() {
        modules.add(new ClickGUIModule());
        modules.add(new ExampleBillboard());
        modules.add(new FontModule());
        modules.add(new HUD());
        modules.add(new BaritoneModule());
    }

}