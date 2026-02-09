package dev.xpple.seedmapper.seedmap;

import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.util.QuartPos2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

// TODO refactor so that the minimap is not a `Screen`; it has no reason to be one
public class MinimapScreen extends SeedMapScreen {

    private boolean initialized = false;
    private int lastWidth = -1;
    private int lastHeight = -1;

    public MinimapScreen(long seed, int dimension, int version, int generatorFlags) {
        super(seed, dimension, version, generatorFlags, Minecraft.getInstance().player.blockPosition(), Minecraft.getInstance().player.getRotationVector());
    }

    public void initForOverlay(int width, int height) {
        if (this.initialized && width == this.lastWidth && height == this.lastHeight) {
            return;
        }
        this.init(width, height);
        this.initialized = true;
        this.lastWidth = width;
        this.lastHeight = height;
    }

    public void renderToHud(GuiGraphics guiGraphics, float partialTick) {
        boolean rotateMinimap = Configs.RotateMinimap;
        int contentWidth = Configs.MinimapWidth;
        int contentHeight = Configs.MinimapHeight;
        int renderContentWidth = contentWidth;
        int renderContentHeight = contentHeight;
        if (rotateMinimap) {
            int diagonal = Mth.ceil(Math.sqrt(contentWidth * contentWidth + contentHeight * contentHeight));
            renderContentWidth = diagonal;
            renderContentHeight = diagonal;
        }
        // ensures super.seedMapWidth == renderContentWidth
        int renderWidth = renderContentWidth + 2 * this.horizontalPadding();
        // ensures super.seedMapHeight == renderContentHeight
        int renderHeight = renderContentHeight + 2 * this.verticalPadding();

        this.initForOverlay(renderWidth, renderHeight);

        guiGraphics.enableScissor(this.horizontalPadding(), this.verticalPadding(), this.horizontalPadding() + contentWidth, this.verticalPadding() + contentHeight);

        var pose = guiGraphics.pose();
        pose.pushMatrix();
        if (rotateMinimap) {
            pose.translate(-this.centerX + (float) (this.horizontalPadding() + contentWidth / 2), -this.centerY + (float) (this.verticalPadding() + contentHeight / 2));
            pose.translate(this.centerX, this.centerY);
            pose.rotate((float) (-Math.toRadians(this.getPlayerRotation().y) + Math.PI));
            pose.translate(-this.centerX, -this.centerY);
        }
        this.renderBiomes(guiGraphics, Integer.MIN_VALUE, Integer.MIN_VALUE, partialTick);

        this.renderFeatures(guiGraphics, Integer.MIN_VALUE, Integer.MIN_VALUE, partialTick);
        pose.popMatrix();

        if (Configs.RotateMinimap) {
            this.drawCenterCross(guiGraphics, this.horizontalPadding() + contentWidth / 2, this.verticalPadding() + contentHeight / 2);
        }

        guiGraphics.disableScissor();
    }

    private void drawCenterCross(GuiGraphics guiGraphics, int centerX, int centerY) {
        int crossHalf = 3;
        int color = 0xFF_FFFFFF;
        guiGraphics.fill(centerX - crossHalf, centerY, centerX + crossHalf + 1, centerY + 1, color);
        guiGraphics.fill(centerX, centerY - crossHalf, centerX + 1, centerY + crossHalf + 1, color);
    }

    public void update(Vec3 pos, Vec2 playerRotation) {
        this.updatePlayerPosition(BlockPos.containing(pos));
        this.updatePlayerRotation(playerRotation);
        this.moveCenter(QuartPos2f.fromVec3(pos));
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    @Override
    protected void drawPlayerIndicator(GuiGraphics guiGraphics) {
        if (!Configs.RotateMinimap) {
            this.drawDirectionArrow(guiGraphics, this.centerX - 10, this.centerY - 10);
        }
    }

    @Override
    protected boolean isMinimap() {
        return true;
    }

    @Override
    protected int horizontalPadding() {
        return Configs.MinimapOffsetX;
    }

    @Override
    protected int verticalPadding() {
        return Configs.MinimapOffsetY;
    }
}
