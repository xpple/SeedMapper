package dev.xpple.seedmapper.seedmap;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ItemIconButton extends Button {

    public static final int ICON_SIZE = 16;

    private final ItemStack item;

    protected ItemIconButton(int x, int y, ItemStack item, Component message, OnPress onPress) {
        super(x, y, ICON_SIZE, ICON_SIZE, message, onPress, DEFAULT_NARRATION);
        this.item = item;
        this.setTooltip(Tooltip.create(message));
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.renderItem(this.item, this.getX(), this.getY());
    }
}
