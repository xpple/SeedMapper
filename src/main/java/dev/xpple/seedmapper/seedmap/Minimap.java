package dev.xpple.seedmapper.seedmap;

import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.util.QuartPos2f;
import dev.xpple.seedmapper.util.SeedIdentifierWithDimension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class Minimap {

    private final SeedMapRenderer seedMapRenderer;

    private boolean initialized = false;
    private int lastWidth = -1;
    private int lastHeight = -1;

    public Minimap(SeedIdentifierWithDimension seedIdentifierWithDimension) {
        BlockPos playerPosition = Minecraft.getInstance().player.blockPosition();
        Minecraft.getInstance().player.getRotationVector();
        this.seedMapRenderer = new SeedMapRenderer(seedIdentifierWithDimension, playerPosition, Minecraft.getInstance().player.getRotationVector(), this::horizontalPadding, this::verticalPadding, true);
    }

    public void initForOverlay(int contentWidth, int contentHeight) {
        if (this.initialized && contentWidth == this.lastWidth && contentHeight == this.lastHeight) {
            return;
        }

        int renderContentWidth = contentWidth;
        int renderContentHeight = contentHeight;
        if (Configs.RotateMinimap) {
            int diagonal = Mth.ceil(Math.sqrt(contentWidth * contentWidth + contentHeight * contentHeight));
            renderContentWidth = diagonal;
            renderContentHeight = diagonal;
        }
        int renderWidth = renderContentWidth + 2 * this.horizontalPadding();
        int renderHeight = renderContentHeight + 2 * this.verticalPadding();

        this.seedMapRenderer.centerX = renderWidth / 2;
        this.seedMapRenderer.centerY = renderHeight / 2;

        this.seedMapRenderer.seedMapWidth = renderContentWidth;
        this.seedMapRenderer.seedMapHeight = renderContentHeight;

        this.initialized = true;
        this.lastWidth = renderWidth;
        this.lastHeight = renderHeight;
    }

    public void renderToHud(GuiGraphicsExtractor guiGraphicsExtractor, float partialTick) {
        boolean rotateMinimap = Configs.RotateMinimap;
        int contentWidth = Configs.MinimapWidth;
        int contentHeight = Configs.MinimapHeight;

        this.initForOverlay(contentWidth, contentHeight);

        guiGraphicsExtractor.enableScissor(this.horizontalPadding(), this.verticalPadding(), this.horizontalPadding() + contentWidth, this.verticalPadding() + contentHeight);

        var pose = guiGraphicsExtractor.pose();
        pose.pushMatrix();
        if (rotateMinimap) {
            pose.translate(-this.seedMapRenderer.centerX + (float) (this.horizontalPadding() + contentWidth / 2), -this.seedMapRenderer.centerY + (float) (this.verticalPadding() + contentHeight / 2));
            pose.translate(this.seedMapRenderer.centerX, this.seedMapRenderer.centerY);
            pose.rotate((float) (-Math.toRadians(this.seedMapRenderer.getPlayerRotation().y) + Math.PI));
            pose.translate(-this.seedMapRenderer.centerX, -this.seedMapRenderer.centerY);
        }
        this.seedMapRenderer.renderBiomes(guiGraphicsExtractor, Integer.MIN_VALUE, Integer.MIN_VALUE, partialTick);
        guiGraphicsExtractor.nextStratum();
        this.seedMapRenderer.renderFeatures(guiGraphicsExtractor, Integer.MIN_VALUE, Integer.MIN_VALUE, partialTick);
        guiGraphicsExtractor.nextStratum();
        if (!Configs.RotateMinimap) {
            this.seedMapRenderer.drawDirectionArrow(guiGraphicsExtractor, this.seedMapRenderer.centerX - 10, this.seedMapRenderer.centerY - 10);
        }
        pose.popMatrix();
        guiGraphicsExtractor.nextStratum();
        if (Configs.RotateMinimap) {
            this.drawCenterCross(guiGraphicsExtractor, this.horizontalPadding() + contentWidth / 2, this.verticalPadding() + contentHeight / 2);
        }

        guiGraphicsExtractor.disableScissor();
    }

    private void drawCenterCross(GuiGraphicsExtractor guiGraphicsExtractor, int centerX, int centerY) {
        int crossHalf = 3;
        int color = 0xFF_FFFFFF;
        guiGraphicsExtractor.fill(centerX - crossHalf, centerY, centerX + crossHalf + 1, centerY + 1, color);
        guiGraphicsExtractor.fill(centerX, centerY - crossHalf, centerX + 1, centerY + crossHalf + 1, color);
    }

    public void update(Vec3 pos, Vec2 playerRotation) {
        this.seedMapRenderer.updatePlayerPosition(BlockPos.containing(pos));
        this.seedMapRenderer.updatePlayerRotation(playerRotation);
        this.seedMapRenderer.moveCenter(QuartPos2f.fromVec3(pos));
    }

    public SeedMapRenderer getSeedMapRenderer() {
        return this.seedMapRenderer;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    private int horizontalPadding() {
        return Configs.MinimapOffsetX;
    }

    private int verticalPadding() {
        return Configs.MinimapOffsetY;
    }

    public void close() {
        this.seedMapRenderer.close();
    }
}
