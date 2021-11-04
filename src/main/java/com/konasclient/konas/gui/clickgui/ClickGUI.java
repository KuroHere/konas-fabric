package com.konasclient.konas.gui.clickgui;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.client.ScrollEvent;
import com.konasclient.konas.gui.clickgui.frame.ButtonFrame;
import com.konasclient.konas.gui.clickgui.frame.CategoryFrame;
import com.konasclient.konas.gui.clickgui.frame.DescriptionFrame;
import com.konasclient.konas.gui.clickgui.frame.Frame;
import com.konasclient.konas.mixin.MouseAccessor;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.modules.client.ClickGUIModule;
import com.konasclient.konas.util.render.gui.GuiRenderWrapper;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;

public class ClickGUI extends Screen {

    private static boolean hudEditor = false;
    private final ArrayList<Frame> frames = new ArrayList<>();
    private boolean initialized = false;
    private boolean binding;
    private int lastMouseButton = 0;
    private long lastClickTime = 0;
    private int scrollY = 0;

    public ClickGUI() {
        super(new TranslatableText("ClickGUI"));
    }

    public static boolean isHudEditor() {
        return hudEditor;
    }

    public static void setHudEditor(boolean hudEditor) {
        ClickGUI.hudEditor = hudEditor;
    }

    public void initialize() {
        int x = 20;
        int y = 20;

        for (Category moduleCategory : Category.values()) {
            getFrames().add(new CategoryFrame(moduleCategory, x, y, 100.0F, 16.0F));
            x += 110;
        }

/*
        getFrames().add(new HudFrame(100F, y, 100.0F, 16.0F));

        for (Container container : KonasGlobals.INSTANCE.containerManager.getContainers()) {
            getFrames().add(new ContainerFrame(container));
        }
*/
        getFrames().add(new ButtonFrame());


        getFrames().forEach(frame -> {
            frame.setExtended(true);
        });

        getFrames().add(new DescriptionFrame());

        getFrames().forEach(Frame::initialize);

        initialized = true;
        binding = false;
    }

    @EventHandler
    public void onScroll(ScrollEvent event) {
        scrollY += (int) (event.scroll * 5D);
    }

    @Override
    public void onClose() {
        Konas.EVENT_BUS.unsubscribe(this);
        getFrames().forEach(frame -> frame.setDragging(false));
        super.onClose();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        GuiRenderWrapper.draw2DGradientRect(0, 0, MinecraftClient.getInstance().getWindow().getScaledWidth(),  MinecraftClient.getInstance().getWindow().getScaledHeight(), ClickGUIModule.overlay.getValue().getColor(), ClickGUIModule.overlayTop.getValue().getColor(), ClickGUIModule.overlay.getValue().getColor(), ClickGUIModule.overlayTop.getValue().getColor());

        RenderSystem.translated(0, scrollY, 0);

        getFrames().forEach(frame -> frame.onRender(mouseX, mouseY - scrollY, delta));

        RenderSystem.translated(0, -scrollY, 0);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!binding && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            super.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }

        getFrames().forEach(frame -> frame.keyPressed(keyCode, scanCode, modifiers));
        return false;
    }

    private boolean mouseDown;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseDown = true;
        boolean used = super.mouseClicked(mouseX, mouseY, button);
        lastClickTime = System.currentTimeMillis();
        lastMouseButton = button;

        for (int i = getFrames().size() - 1; i >= 0; i--) {
            Frame frame = getFrames().get(i);
            if (frame.onMouseClicked((int) mouseX, (int) mouseY - scrollY, button)) {
                Collections.swap(getFrames(), i, getFrames().size() - 2);
                return true;
            }
        }

        return used;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        mouseDown = false;
        boolean used = super.mouseReleased(mouseX, mouseY, mouseButton);
        getFrames().forEach(frame -> frame.onMouseReleased((int) mouseX, (int) mouseY - scrollY, mouseButton));
        for (Frame frame : getFrames()) {
            for (Frame otherFrame : getFrames()) {
                if (frame == otherFrame) continue;
                if (frame.getPosX() == otherFrame.getPosX() && frame.getPosY() == otherFrame.getPosY()) {
                    otherFrame.setPosX(otherFrame.getPosX() + 10F);
                    otherFrame.setPosY(otherFrame.getPosY() + 10F);
                    if (otherFrame instanceof CategoryFrame) {
                        ((CategoryFrame) otherFrame).getEnabledComponents().forEach(component -> component.onMove(otherFrame.getPosX(), otherFrame.getPosY()));
                    }
                }
            }
        }
        return used;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (mouseDown) {
            getFrames().forEach(frame -> frame.onMouseClickMove((int) mouseX, (int) mouseY - scrollY, lastMouseButton, System.currentTimeMillis() - lastClickTime));
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !binding;
    }

    public ArrayList<Frame> getFrames() {
        return this.frames;
    }

    public void setBinding(boolean binding) {
        this.binding = binding;
    }

}
