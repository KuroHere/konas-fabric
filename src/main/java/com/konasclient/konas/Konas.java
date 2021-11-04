package com.konasclient.konas;

import com.konasclient.konas.command.CommandManager;
import com.konasclient.konas.event.events.network.PacketEvent;
import com.konasclient.konas.event.events.world.UpdateEvent;
import com.konasclient.konas.event.listeners.KeyListener;
import com.konasclient.konas.gui.clickgui.ClickGUI;
import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.util.EChestMemory;
import com.konasclient.konas.util.action.ActionManager;
import com.konasclient.konas.util.client.TickRateUtil;
import com.konasclient.konas.util.config.Config;
import com.konasclient.konas.util.config.ShutdownHook;
import com.konasclient.konas.util.math.MatrixUtil;
import com.konasclient.konas.util.render.KonasRenderLayers;
import com.konasclient.konas.util.render.Matrices;
import com.konasclient.konas.util.render.WorldOutlineRenderLayers;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Konas implements ModInitializer {

    public static final String NAME = "KonasFabric";
    public static final String VERSION = "0.1";
    public static final String PREFIX = ".";

    public static final Logger LOG = LogManager.getLogger(Konas.NAME);

    public static final IEventBus EVENT_BUS = new EventBus();

    public static MinecraftClient mc;

    public static Frustum currentFrustum;

    public static ClickGUI clickGUI;

    public static boolean isTickRunning = false;

    public static float prevYaw = 0F;
    public static float prevPitch = 0F;

    @Override
    public void onInitialize() {
        LOG.info("Initialising Konas");
        long startTime = System.currentTimeMillis();

        mc = MinecraftClient.getInstance();

        KonasRenderLayers.INSTANCE = new KonasRenderLayers();
        WorldOutlineRenderLayers.INSTANCE = new WorldOutlineRenderLayers();

        Matrices.begin(new MatrixStack());

        ActionManager actionManager = new ActionManager();
        EVENT_BUS.subscribe(actionManager);

        EVENT_BUS.subscribe(this);

        ModuleManager.init();
        CommandManager.init();

        KeyListener keyListener = new KeyListener();
        EVENT_BUS.subscribe(keyListener);

        clickGUI = new ClickGUI();
        clickGUI.initialize();

        TickRateUtil.reset();

        Config.load(Config.getLastModified());

        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        EChestMemory.init();

        MatrixUtil matrixUtil = new MatrixUtil();
        EVENT_BUS.subscribe(matrixUtil);

        LOG.info("Initialized Konas, took " + (System.currentTimeMillis() - startTime) + "ms");
    }

    @EventHandler
    public void onUpdate(UpdateEvent.Pre event) {
        isTickRunning = true;
    }

    @EventHandler
    public void onUpdate(UpdateEvent.Post event) {
        isTickRunning = false;
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof WorldTimeUpdateS2CPacket) {
            TickRateUtil.onTimeUpdate();
        }
    }

    public static <T> T postEvent(T event) {
        EVENT_BUS.post(event);
        return event;
    }
}