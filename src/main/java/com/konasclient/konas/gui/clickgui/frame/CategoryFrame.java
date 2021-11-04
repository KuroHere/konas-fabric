package com.konasclient.konas.gui.clickgui.frame;

import com.konasclient.konas.Konas;
import com.konasclient.konas.gui.clickgui.ClickGUI;
import com.konasclient.konas.gui.clickgui.component.Component;
import com.konasclient.konas.gui.clickgui.component.ModuleComponent;
import com.konasclient.konas.mixin.MouseAccessor;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.client.ClickGUIModule;
import com.konasclient.konas.util.render.font.FontRenderWrapper;
import com.konasclient.konas.util.render.gui.GuiRenderWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.Window;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.Comparator;

public class CategoryFrame extends Frame {

    private final Category moduleCategory;

    private final ArrayList<Component> components = new ArrayList<>();

    private int enabledTicks = 0;

    private boolean shouldDisable = false;

    public CategoryFrame(Category moduleCategory, float posX, float posY, float width, float height) {
        super(moduleCategory.name(), posX, posY, width, height);
        this.moduleCategory = moduleCategory;
    }

    @Override
    public void initialize() {
        float offsetY = getHeight();

        ArrayList<Module> modulesList = ModuleManager.getFromCategory(getModuleCategory());
        modulesList.sort(Comparator.comparing(Module::getName));

        for (Module module : modulesList) {
            getComponents().add(new ModuleComponent(module, getPosX(), getPosY(), 2.0F, offsetY, getWidth() - 4.0F, 14.0F));
            offsetY += 14.0F;
        }

        getComponents().forEach(Component::initialize);
    }

    @Override
    public void onMove(float posX, float posY) {
        components.forEach(component -> component.onMove(posX, posY));
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        if (ClickGUI.isHudEditor()) return;

        // Dragging
        if (isDragging()) {
            setPosX(mouseX + getPrevPosX());
            setPosY(mouseY + getPrevPosY());
            getEnabledComponents().forEach(component -> component.onMove(getPosX(), getPosY()));
        }

        // This is very important, to maintain bounds within screen
        Window window = MinecraftClient.getInstance().getWindow();

        if (getPosX() < 0.0F) {
            setPosX(0.0F);
            getEnabledComponents().forEach(component -> component.onMove(getPosX(), getPosY()));
        }

        if (getPosX() + getWidth() > window.getScaledWidth()) {
            setPosX(window.getScaledWidth() - getWidth());
            getEnabledComponents().forEach(component -> component.onMove(getPosX(), getPosY()));
        }

        if (getPosY() < 0.0F) {
            setPosY(0.0F);
            getEnabledComponents().forEach(component -> component.onMove(getPosX(), getPosY()));
        }

        if (getPosY() + getHeight() + getTotalHeight() > window.getScaledHeight()) {
            setPosY(window.getScaledHeight() - getHeight() - getTotalHeight());
            getEnabledComponents().forEach(component -> component.onMove(getPosX(), getPosY()));
        }

        // Title/Background
        GuiRenderWrapper.drawRect(getPosX(), getPosY(), getWidth(), getHeight(), ClickGUIModule.header.getValue().getColor());
        FontRenderWrapper.drawStringWithShadow(getName(), (int) (getPosX() + 3.0F), (int) (getPosY() + getHeight() / 2 - (FontRenderWrapper.getFontHeight() / 2) - 0.5F), ClickGUIModule.font.getValue().getColor());

        // Scrolling
        if (isExtended()) {
            // Scissor for scrolling
            //TODO: Fix scissoring??
            // GL11.glPushMatrix();
//            GL11.glEnable(GL_SCISSOR_TEST);
            if (ClickGUIModule.animate.getValue()) {
//                GuiRenderHelper.prepareScissorBox(window, getPosX(), getPosY() + getHeight(), getWidth(), Math.min(ClickGUIModule.height.getValue(), ClickGUIModule.height.getValue() * ((float) enabledTicks / (float) ClickGUIModule.animationSpeed.getValue())));
                if (shouldDisable) {
                    enabledTicks--;
                    if (enabledTicks <= 0) {
                        super.setExtended(false);
                        shouldDisable = false;
                    }
                } else if (enabledTicks < ClickGUIModule.animationSpeed.getValue()) {
                    enabledTicks++;
                }
            } else {
//                GuiRenderHelper.prepareScissorBox(window, getPosX(), getPosY() + getHeight(), getWidth(), ClickGUIModule.height.getValue());
                if (shouldDisable) {
                    super.setExtended(false);
                    shouldDisable = false;
                }
            }
            GuiRenderWrapper.drawRect(getPosX(), getPosY() + getHeight(), getWidth(), getTotalHeight() + 2, ClickGUIModule.background.getValue().getColor());
            getEnabledComponents().forEach(component -> component.onRender(mouseX, mouseY, partialTicks));
//            GL11.glDisable(GL_SCISSOR_TEST);
            if (ClickGUIModule.outline.getValue() && !ClickGUIModule.animate.getValue()) {
                float thick = (float) ClickGUIModule.thickness.getValue();
                GuiRenderWrapper.drawRect(getPosX() - thick, getPosY() - thick, thick, getHeight() - thick + getTotalHeight() + thick * 2F, ClickGUIModule.color.getValue().getColor());
                GuiRenderWrapper.drawRect(getPosX(), getPosY() - thick, getWidth(), thick, ClickGUIModule.color.getValue().getColor());
                GuiRenderWrapper.drawRect(getPosX() + getWidth(), getPosY() - thick, thick, getHeight() - thick + getTotalHeight() + thick * 2F, ClickGUIModule.color.getValue().getColor());
                GuiRenderWrapper.drawRect(getPosX(), getPosY() + getHeight() + getTotalHeight() - thick, getWidth(), thick, ClickGUIModule.color.getValue().getColor());
            }
            // GL11.glPopMatrix();
        } else if (ClickGUIModule.outline.getValue() && !ClickGUIModule.animate.getValue()) {
            float thick = (float) ClickGUIModule.thickness.getValue();
            GuiRenderWrapper.drawRect(getPosX() - thick, getPosY() - thick, thick, getHeight() + thick * 2F, ClickGUIModule.color.getValue().getColor());
            GuiRenderWrapper.drawRect(getPosX(), getPosY() - thick, getWidth(), thick, ClickGUIModule.color.getValue().getColor());
            GuiRenderWrapper.drawRect(getPosX() + getWidth(), getPosY() - thick, thick, getHeight() + thick * 2F, ClickGUIModule.color.getValue().getColor());
            GuiRenderWrapper.drawRect(getPosX(), getPosY() + getHeight(), getWidth(), thick, ClickGUIModule.color.getValue().getColor());
        }

        updateComponentOffsets(mouseX, mouseY);
    }

    private void updateComponentOffsets(int mouseX, int mouseY) {
        float offsetY = getHeight();

        for (Component component : getEnabledComponents()) {
            component.setOffsetY(offsetY);
            component.onMove(getPosX(), getPosY());
            if (component instanceof ModuleComponent) {
                ((ModuleComponent) component).handleDescription(mouseX, mouseY);
                if (component.isExtended()) {
                    for (Component child : ((ModuleComponent) component).getEnabledComponents())
                        offsetY += child.getHeight();
                }
            }
            offsetY += component.getHeight();
        }
    }

    @Override
    public void setExtended(boolean extended) {
        if (extended) {
            enabledTicks = 0;
            super.setExtended(extended);
        } else {
            shouldDisable = true;
            enabledTicks = ClickGUIModule.animationSpeed.getValue();
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ClickGUI.isHudEditor()) return;
        if (isExtended()) {
            getEnabledComponents().forEach(component -> component.keyPressed(keyCode, scanCode, modifiers));
        }
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.onMouseClicked(mouseX, mouseY, mouseButton);
        if (ClickGUI.isHudEditor()) return false;
        boolean withinBounds = mouseWithinBounds(mouseX, mouseY, getPosX(), getPosY(), getWidth(), getHeight());

        switch (mouseButton) {
            case 0: {
                if (withinBounds) {
                    setDragging(true);
                    setPrevPosX(getPosX() - mouseX);
                    setPrevPosY(getPosY() - mouseY);
                    return true;
                }
                break;
            }
            case 1: {
                if (withinBounds) {
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    setExtended(!isExtended());
                    return true;
                }
                break;
            }
        }

        if (isExtended() && mouseWithinBounds(mouseX, mouseY, getPosX(), (getPosY() + getHeight()), getWidth(), getTotalHeight())) {
            for (Component component : getEnabledComponents()) {
                if (component.onMouseClicked(mouseX, mouseY, mouseButton)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.onMouseReleased(mouseX, mouseY, mouseButton);
        if (ClickGUI.isHudEditor()) return;
        if (mouseButton == 0 && isDragging()) {
            setDragging(false);
        }

        if (isExtended()) {
            getEnabledComponents().forEach(component -> component.onMouseReleased(mouseX, mouseY, mouseButton));
        }
    }

    @Override
    public void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.onMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (ClickGUI.isHudEditor()) return;
        if (isExtended()) {
            getEnabledComponents().forEach(component -> component.onMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick));
        }
    }

    private float getTotalHeight() {
        float currentHeight = 0.0F;
        if (!isExtended()) return currentHeight;

        for (Component component : getEnabledComponents()) {
            currentHeight += component.getHeight();

            if (component instanceof ModuleComponent && component.isExtended()) {
                for (Component child : ((ModuleComponent) component).getEnabledComponents()) {
                    currentHeight += child.getHeight();
                }
            }
        }

        return currentHeight;
    }

    public Category getModuleCategory() {
        return moduleCategory;
    }

    public ArrayList<Component> getComponents() {
        return components;
    }

    public ArrayList<Component> getEnabledComponents() {
        ArrayList<Component> enabledComponents = new ArrayList<>();
        for (Component component : getComponents()) {
            enabledComponents.add(component);
        }
        return enabledComponents;
    }
}