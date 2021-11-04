package com.konasclient.konas.gui.clickgui.component;

import com.konasclient.konas.gui.clickgui.frame.DescriptionFrame;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.module.modules.client.ClickGUIModule;
import com.konasclient.konas.setting.*;
import com.konasclient.konas.util.StringUtils;
import com.konasclient.konas.util.client.Timer;
import com.konasclient.konas.util.render.font.FontRenderWrapper;
import com.konasclient.konas.util.render.gui.GuiRenderWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

import java.awt.*;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;


public class ModuleComponent extends Component {
    private final Module module;

    private final ArrayList<Component> components = new ArrayList<>();

    private final Timer hoverTimer = new Timer();

    public ModuleComponent(Module module, float parentX, float parentY, float offsetX, float offsetY, float width, float height) {
        super(module.getTitle(), parentX, parentY, offsetX, offsetY, width, height);
        this.module = module;
    }

    @Override
    public void initialize() {
        super.initialize();

        float childOffsetY = getHeight();

        for (Setting setting : getModule().getSettingList()) {
            float tempXOffset = setting.hasParent() ? 4F : 2F;

            if (setting.getValue() instanceof Boolean) {
                getComponents().add(new BooleanSettingComponent(setting, getAbsoluteX(), getAbsoluteY(), tempXOffset, childOffsetY, getWidth() - tempXOffset, 12.0F));
                childOffsetY += 12F;
            } else if (setting.getValue() instanceof Bind) {
                getComponents().add(new BindSettingComponent(getModule(), getAbsoluteX(), getAbsoluteY(), tempXOffset, childOffsetY, getWidth() - tempXOffset, 12.0F));
                childOffsetY += 12F;
            } else if (setting.getValue() instanceof Parent) {
                getComponents().add(new ParentSettingComponent(setting, getAbsoluteX(), getAbsoluteY(), tempXOffset, childOffsetY, getWidth() - tempXOffset, 12.0F));
                childOffsetY += 12F;
            } else if (setting.getValue() instanceof Number) {
                getComponents().add(new SliderSettingComponent(setting, getAbsoluteX(), getAbsoluteY(), tempXOffset, childOffsetY, getWidth() - tempXOffset, 12.0F));
                childOffsetY += 12F;
            } else if (setting.getValue() instanceof Enum) {
                getComponents().add(new EnumSettingComponent(setting, getAbsoluteX(), getAbsoluteY(), tempXOffset, childOffsetY, getWidth() - tempXOffset, 12.0F));
                childOffsetY += 12F;
            } else if (setting.getValue() instanceof ColorSetting) {
                getComponents().add(new ColorSettingComponent(setting, getAbsoluteX(), getAbsoluteY(), tempXOffset, childOffsetY, getWidth() - tempXOffset, 60F));
                childOffsetY += 12F;
            } else if (setting.getValue() instanceof SubBind) {
                getComponents().add(new SubBindSettingComponent(setting, getAbsoluteX(), getAbsoluteY(), tempXOffset, childOffsetY, getWidth() - tempXOffset, 12.0F));
                childOffsetY += 12F;
            }
        }

        getEnabledComponents().forEach(Component::initialize);
    }

    @Override
    public void onMove(float parentX, float parentY) {
        super.onMove(parentX, parentY);
        getEnabledComponents().forEach(component -> component.onMove(getAbsoluteX(), getAbsoluteY()));
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);

        float childOffsetY = getHeight();

        for (Component component : getEnabledComponents()) {
            component.setOffsetY(childOffsetY);
            childOffsetY += component.getHeight();
        }

        int color = module.isActive() ?
                ClickGUIModule.color.getValue().getColor() :
                ClickGUIModule.secondary.getValue().getColor();
        if (ClickGUIModule.hover.getValue() && mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight())) {
            if (module.isActive()) {
                color = ClickGUIModule.color.getValue().getColorObject().brighter().hashCode();
            } else {
                color = new Color(96, 96, 96, 100).hashCode();
            }
        }

        GuiRenderWrapper.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(), color);
        FontRenderWrapper.drawStringWithShadow(getName(), (int) (getAbsoluteX() + 4.0F), (int) (getAbsoluteY() + getHeight() / 2.0F - (FontRenderWrapper.getStringHeight(getName()) / 2)), ClickGUIModule.font.getValue().getColor());

        if (module.getKeybind() != -1 && ClickGUIModule.binds.getValue()) {
            String keyName = StringUtils.getKeyName(module.getKeybind());
            FontRenderWrapper.drawStringWithShadow(keyName, (int) (getAbsoluteX() + getWidth() - 4F - FontRenderWrapper.getStringWidth(keyName)), (int) (getAbsoluteY() + getHeight() / 2.0F - (FontRenderWrapper.getStringHeight(keyName) / 2)), ClickGUIModule.font.getValue().getColor());
        }

        if (isExtended()) {
            getEnabledComponents().forEach(component -> component.onRender(mouseX, mouseY, partialTicks));
        }
    }

    public void handleDescription(int mouseX, int mouseY) {
        if (mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight())) {
            if (hoverTimer.hasPassed(500)) {
                DescriptionFrame.desc = getModule().getDescription();
            }
        } else {
            if (DescriptionFrame.desc != null && DescriptionFrame.desc.equals(getModule().getDescription())) {
                DescriptionFrame.desc = null;
            }
            hoverTimer.reset();
        }

    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isExtended())
            getEnabledComponents().forEach(component -> component.keyPressed(keyCode, scanCode, modifiers));
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.onMouseClicked(mouseX, mouseY, mouseButton);

        boolean hovered = mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight());

        if (hovered) {
            switch (mouseButton) {
                case GLFW_MOUSE_BUTTON_LEFT:
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    getModule().toggle();
                    return true;
                case GLFW_MOUSE_BUTTON_RIGHT:
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    setExtended(!isExtended());
                    return true;
            }
        }

        if (isExtended()) {
            for (Component component : getEnabledComponents()) {
                if (component.onMouseClicked(mouseX, mouseY, mouseButton)) return true;
            }
        }

        return false;
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.onMouseReleased(mouseX, mouseY, mouseButton);

        if (isExtended())
            getEnabledComponents().forEach(component -> component.onMouseReleased(mouseX, mouseY, mouseButton));
    }

    @Override
    public void onMouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        super.onMouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);

        if (isExtended())
            getEnabledComponents().forEach(component -> component.onMouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick));
    }

    public Module getModule() {
        return module;
    }

    public ArrayList<Component> getComponents() {
        return components;
    }

    public ArrayList<Component> getEnabledComponents() {
        ArrayList<Component> enabledComponents = new ArrayList<>();
        for (Component component : getComponents()) {

            if (component instanceof BooleanSettingComponent) {
                if (((BooleanSettingComponent) component).getBooleanSetting().isVisible())
                    enabledComponents.add(component);
            } else if (component instanceof BindSettingComponent) enabledComponents.add(component);

            else if (component instanceof EnumSettingComponent) {
                if (((EnumSettingComponent) component).getEnumSetting().isVisible()) enabledComponents.add(component);
            } else if (component instanceof SliderSettingComponent) {
                if (((SliderSettingComponent) component).getNumberSetting().isVisible())
                    enabledComponents.add(component);
            } else if (component instanceof ColorSettingComponent) {
                if (((ColorSettingComponent) component).getSetting().isVisible()) {
                    enabledComponents.add(component);
                }
            } else {
                enabledComponents.add(component);
            }
        }
        return enabledComponents;
    }

}
