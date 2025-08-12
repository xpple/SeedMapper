package dev.xpple.seedmapper.seedmap;

import com.github.cubiomes.Cubiomes;
import com.mojang.blaze3d.platform.InputConstants;
import dev.xpple.seedmapper.SeedMapper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static dev.xpple.seedmapper.util.ChatBuilder.*;

public class ChestLootWidget {

    private static final ResourceLocation CHEST_CONTAINER = ResourceLocation.fromNamespaceAndPath(SeedMapper.MOD_ID, "textures/gui/chest_container.png");
    private static final int CHEST_CONTAINER_WIDTH = 176;
    private static final int CHEST_CONTAINER_HEIGHT = 78;

    private static final ResourceLocation BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath("fabric", "textures/gui/creative_buttons.png");
    private static final int BUTTON_X_OFFSET = 149;
    private static final int BUTTON_Y_OFFSET = 4;
    private static final int BUTTON_WIDTH = 10;
    private static final int BUTTON_HEIGHT = 12;

    private static final int ITEM_SLOT_SIZE = 18;

    private boolean active = false;

    private int x = 0;
    private int y = 0;

    private int chestIndex = 0;
    private List<ChestLootData> chestDataList = new ArrayList<>();

    private List<List<ClientTooltipComponent>> extraChestInfo = new ArrayList<>();

    public void setContent(int x, int y, List<ChestLootData> chestDataList) {
        this.active = true;

        this.x = x;
        this.y = y;

        this.chestDataList = chestDataList;
        for (ChestLootData chestData : this.chestDataList) {
            List<ClientTooltipComponent> tooltips = new ArrayList<>();
            Component pieceNameComponent = Component.translatable("seedMap.chestLoot.extraInfo.pieceName", accent(chestData.pieceName()));
            tooltips.add(ClientTooltipComponent.create(pieceNameComponent.getVisualOrderText()));
            BlockPos chestPos = chestData.chestPos();
            Component chestPosComponent = Component.translatable("seedMap.chestLoot.extraInfo.chestPos", accent("x: %d, z: %d".formatted(chestPos.getX(), chestPos.getZ())));
            tooltips.add(ClientTooltipComponent.create(chestPosComponent.getVisualOrderText()));
            String lootTable = chestData.lootTable();
            Component lootTableComponent = Component.translatable("seedMap.chestLoot.extraInfo.lootTable", accent(lootTable));
            tooltips.add(ClientTooltipComponent.create(lootTableComponent.getVisualOrderText()));
            long lootSeed = chestData.lootSeed();
            Component lootSeedComponent = Component.translatable("seedMap.chestLoot.extraInfo.lootSeed", accent(Long.toString(lootSeed)));
            tooltips.add(ClientTooltipComponent.create(lootSeedComponent.getVisualOrderText()));
            this.extraChestInfo.add(tooltips);
        }
    }

    public void clear() {
        this.active = false;

        this.x = 0;
        this.y = 0;
        this.chestIndex = 0;
        this.chestDataList.clear();
        this.extraChestInfo.clear();
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, Font font) {
        if (!this.active) {
            return;
        }
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CHEST_CONTAINER, this.x, this.y, 0, 0, CHEST_CONTAINER_WIDTH, CHEST_CONTAINER_HEIGHT, CHEST_CONTAINER_WIDTH, CHEST_CONTAINER_HEIGHT);

        ChestLootData chestData = this.chestDataList.get(this.chestIndex);
        String structure = Cubiomes.struct2str(chestData.structure()).getString(0);
        Component title = Component.translatable("seedMap.chestLoot.title", structure, this.chestIndex + 1, this.chestDataList.size());

        int minX = this.x + 8;
        int minY = this.y + 6;
        guiGraphics.drawString(font, title, minX, minY, -1);

        int titleWidth = font.width(title.getVisualOrderText());
        if (mouseX >= minX && mouseX <= minX + titleWidth && mouseY >= minY && mouseY <= minY + font.lineHeight) {
            List<ClientTooltipComponent> tooltips = this.extraChestInfo.get(this.chestIndex);
            guiGraphics.renderTooltip(font, tooltips, minX - 4 - 12, this.y - tooltips.size() * font.lineHeight - 8 + 12, DefaultTooltipPositioner.INSTANCE, null);
        }

        minY += 12;
        for (int row = 0; row < 3; row++) {
            int y = minY + row * ITEM_SLOT_SIZE;
            for (int column = 0; column < 9; column++) {
                ItemStack item = chestData.container().getItem(row * 9 + column);
                if (item == ItemStack.EMPTY) {
                    continue;
                }
                int x = minX + column * ITEM_SLOT_SIZE;
                guiGraphics.renderItem(item, x, y);
                guiGraphics.renderItemDecorations(font, item, x, y);
                if (mouseX >= x && mouseX <= x + ITEM_SLOT_SIZE && mouseY >= y && mouseY <= y + ITEM_SLOT_SIZE) {
                    guiGraphics.setTooltipForNextFrame(font, item, mouseX, mouseY);
                }
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
            this.chestIndex = Math.max(0, this.chestIndex - 1);
            return true;
        }
        minX = minX + BUTTON_WIDTH;
        maxX = maxX + BUTTON_WIDTH;
        if (mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY) {
            this.chestIndex = Math.min(this.chestDataList.size() - 1, this.chestIndex + 1);
            return true;
        }
        return false;
    }
}
