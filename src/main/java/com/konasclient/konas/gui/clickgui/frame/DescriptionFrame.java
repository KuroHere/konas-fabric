package com.konasclient.konas.gui.clickgui.frame;

import com.konasclient.konas.util.render.font.FontRenderWrapper;
import com.konasclient.konas.util.render.gui.GuiRenderWrapper;

import java.awt.*;

public class DescriptionFrame extends Frame {

    public static String desc = null;

    public DescriptionFrame() {
        super("Description", 0, 0, 0, 0);
        this.setExtended(false);
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        if (desc != null) {
            float width = FontRenderWrapper.getStringWidth(desc) + 4;
            float height = FontRenderWrapper.getStringHeight(desc) + 4;
            setPosX(mouseX);
            setPosY(mouseY - height);
            setWidth(width);
            setHeight(height);
            GuiRenderWrapper.drawRect(mouseX, mouseY - height, width, height, Color.BLACK.hashCode());
            FontRenderWrapper.drawString(desc, mouseX + 2, mouseY - height + 2, Color.WHITE.hashCode());
        }
    }

}
