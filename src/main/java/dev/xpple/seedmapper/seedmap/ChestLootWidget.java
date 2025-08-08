package dev.xpple.seedmapper.seedmap;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xpple.seedmapper.SeedMapper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ChestLootWidget {

    private static final ResourceLocation CHEST_CONTAINER = ResourceLocation.fromNamespaceAndPath(SeedMapper.MOD_ID, "textures/gui/chest_container.png");
    private static final int CHEST_CONTAINER_WIDTH = 176;
    private static final int CHEST_CONTAINER_HEIGHT = 78;

    private static final ResourceLocation BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath("fabric", "textures/gui/creative_buttons.png");
    private static final int BUTTON_X_OFFSET = 149;
    private static final int BUTTON_Y_OFFSET = 4;
    private static final int BUTTON_WIDTH = 10;
    private static final int BUTTON_HEIGHT = 12;

    private boolean active = false;

    private int x = 0;
    private int y = 0;
    private String structure = "";
    private int containerIndex = 0;
    private List<SimpleContainer> containers = new ArrayList<>();

    public void setContent(int x, int y, String structure, List<SimpleContainer> containers) {
        this.active = true;

        this.x = x;
        this.y = y;
        this.structure = structure;
        this.containers = containers;
    }

    public void clear() {
        this.active = false;

        this.x = 0;
        this.y = 0;
        this.structure = "";
        this.containerIndex = 0;
        this.containers.clear();
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, Font font) {
        if (!this.active) {
            return;
        }
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CHEST_CONTAINER, this.x, this.y, 0, 0, CHEST_CONTAINER_WIDTH, CHEST_CONTAINER_HEIGHT, CHEST_CONTAINER_WIDTH, CHEST_CONTAINER_HEIGHT);
        Component title = Component.translatable("seedMap.chestLoot", this.structure, this.containerIndex + 1, containers.size());
        int minX = this.x + 8;
        int minY = this.y + 6;
        guiGraphics.drawString(font, title, minX, minY, -1);
        SimpleContainer container = this.containers.get(this.containerIndex);
        minY += 12;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                ItemStack item = container.getItem(row * 9 + column);
                guiGraphics.renderItem(item, minX + column * 18, minY + row * 18);
                guiGraphics.renderItemDecorations(font, item, minX + column * 18, minY + row * 18);
            }
        }

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BUTTON_TEXTURE, this.x + BUTTON_X_OFFSET, this.y + BUTTON_Y_OFFSET, 0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BUTTON_TEXTURE, this.x + BUTTON_X_OFFSET + BUTTON_WIDTH, this.y + BUTTON_Y_OFFSET, BUTTON_WIDTH, 0, BUTTON_WIDTH, BUTTON_HEIGHT, 256, 256);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active) {
            return false;
        }
        if (button != InputConstants.MOUSE_BUTTON_LEFT) {
            return false;
        }
        int minX = this.x + BUTTON_X_OFFSET;
        int minY = this.y + BUTTON_Y_OFFSET;
        int maxX = minX + BUTTON_WIDTH;
        int maxY = minY + BUTTON_HEIGHT;
        if (mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY) {
            this.containerIndex = Math.max(0, this.containerIndex - 1);
            return true;
        }
        minX = minX + BUTTON_WIDTH;
        maxX = maxX + BUTTON_WIDTH;
        if (mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY) {
            this.containerIndex = Math.min(this.containers.size() - 1, this.containerIndex + 1);
            return true;
        }
        return false;
    }
}
