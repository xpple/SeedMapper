package dev.xpple.seedmapper.seedmap;

import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.util.QuartPos2;
import dev.xpple.seedmapper.util.QuartPos2f;
import net.minecraft.world.phys.Vec2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2fStack;

public class SeedMapMinimapScreen extends SeedMapScreen {

    private boolean initialized = false;
    private int cachedWidth = -1;
    private int cachedHeight = -1;

    public SeedMapMinimapScreen(long seed, int dimension, int version, BlockPos playerPos) {
        super(seed, dimension, version, playerPos, new Vec2(0.0F, 0.0F));
    }

    public void initForOverlay(Minecraft minecraft, int width, int height) {
        if (this.initialized && width == this.cachedWidth && height == this.cachedHeight) {
            return;
        }
        this.init(width, height);
        this.initialized = true;
        this.cachedWidth = width;
        this.cachedHeight = height;
    }

    public void renderToHud(GuiGraphics guiGraphics, LocalPlayer player, float partialTick) {
        this.refreshPixelsPerBiome();
        boolean rotateWithPlayer = Configs.SeedMapMinimapRotateWithPlayer;
        int configuredWidth = Math.max(64, Configs.SeedMapMinimapWidth);
        int configuredHeight = Math.max(64, Configs.SeedMapMinimapHeight);
        int contentWidth = Math.max(32, configuredWidth - 2 * horizontalPadding());
        int contentHeight = Math.max(32, configuredHeight - 2 * verticalPadding());
        int renderContentWidth = contentWidth;
        int renderContentHeight = contentHeight;
        if (rotateWithPlayer) {
            int diagonal = Mth.ceil(Math.sqrt(contentWidth * contentWidth + contentHeight * contentHeight));
            renderContentWidth = diagonal;
            renderContentHeight = diagonal;
        }
        int renderWidth = renderContentWidth + 2 * horizontalPadding();
        int renderHeight = renderContentHeight + 2 * verticalPadding();
        this.initForOverlay(Minecraft.getInstance(), renderWidth, renderHeight);

        int offsetX = Configs.SeedMapMinimapOffsetX;
        int offsetY = Configs.SeedMapMinimapOffsetY;
        double extraWidth = renderContentWidth - contentWidth;
        double extraHeight = renderContentHeight - contentHeight;
        double translateX = offsetX - horizontalPadding() - extraWidth / 2.0;
        double translateY = offsetY - verticalPadding() - extraHeight / 2.0;
        double centerX = offsetX + contentWidth / 2.0;
        double centerY = offsetY + contentHeight / 2.0;
        float rotationRadians = rotateWithPlayer ? -this.getMapRotation(player, partialTick) : 0.0F;

        float opacity = (float) Mth.clamp(Configs.SeedMapMinimapOpacity, 0.0D, 1.0D);

        guiGraphics.enableScissor(offsetX, offsetY, offsetX + contentWidth, offsetY + contentHeight);

        this.setFeatureIconRenderingEnabled(false);
        this.setMarkerRenderingEnabled(false);
        this.setPlayerIconRenderingEnabled(false);

        var pose = guiGraphics.pose();
        pose.pushMatrix();
        if (rotateWithPlayer) {
            pose.translate((float) centerX, (float) centerY);
            pose.rotate(rotationRadians);
            pose.translate((float) -centerX, (float) -centerY);
        }
        pose.translate((float) translateX, (float) translateY);
        this.getFeatureWidgets().clear();
        this.renderSeedMap(guiGraphics, Integer.MIN_VALUE, Integer.MIN_VALUE, partialTick);
        pose.popMatrix();

        boolean drawIcons = true;
        this.setFeatureIconRenderingEnabled(drawIcons);
        this.setMarkerRenderingEnabled(drawIcons);
        this.setPlayerIconRenderingEnabled(drawIcons);

        if (drawIcons) {
            this.renderMinimapIcons(guiGraphics, translateX, translateY, centerX, centerY, rotationRadians);
            if (rotateWithPlayer) {
                this.drawCenterCross(guiGraphics, centerX, centerY);
            } else {
                this.drawCenteredPlayerDirectionArrow(guiGraphics, centerX, centerY, 6.0D, partialTick);
            }
        }

        guiGraphics.disableScissor();
    }

    private float getMapRotation(LocalPlayer player, float partialTick) {
        Vec3 look = player.getViewVector(partialTick);
        double dirX = look.x;
        double dirZ = look.z;
        double len = Math.hypot(dirX, dirZ);
        if (len < 1.0E-4D) {
            return 0.0F;
        }
        double normX = dirX / len;
        double normZ = dirZ / len;
        return (float) Math.atan2(normX, -normZ);
    }

    private void renderMinimapIcons(GuiGraphics guiGraphics, double translateX, double translateY, double centerX, double centerY, float rotationRadians) {
        double cos = Math.cos(rotationRadians);
        double sin = Math.sin(rotationRadians);
        double iconScale = Configs.SeedMapMinimapIconScale;
        for (FeatureWidget widget : this.getFeatureWidgets()) {
            MapFeature.Texture texture = widget.texture();
            int scaledWidth = (int) Math.max(1, Math.round(texture.width() * iconScale));
            int scaledHeight = (int) Math.max(1, Math.round(texture.height() * iconScale));
            double baseCenterX = widget.drawX() + texture.width() / 2.0;
            double baseCenterY = widget.drawY() + texture.height() / 2.0;
            double shiftedX = baseCenterX + translateX;
            double shiftedY = baseCenterY + translateY;
            double dx = shiftedX - centerX;
            double dy = shiftedY - centerY;
            double rotatedX = centerX + dx * cos - dy * sin;
            double rotatedY = centerY + dx * sin + dy * cos;
            int drawX = (int) Math.round(rotatedX - scaledWidth / 2.0);
            int drawY = (int) Math.round(rotatedY - scaledHeight / 2.0);
            this.drawFeatureIcon(guiGraphics, texture, drawX, drawY, scaledWidth, scaledHeight, 0xFF_FFFFFF);
        }

        FeatureWidget marker = this.getMarkerWidget();
        if (marker != null && marker.withinBounds()) {
            this.renderSingleIcon(guiGraphics, marker, translateX, translateY, centerX, centerY, cos, sin, 0xFF_FFFFFF, iconScale);
        }
    }

    private void renderSingleIcon(GuiGraphics guiGraphics, FeatureWidget widget, double translateX, double translateY, double centerX, double centerY, double cos, double sin, int colour, double iconScale) {
        MapFeature.Texture texture = widget.texture();
        int scaledWidth = (int) Math.max(1, Math.round(texture.width() * iconScale));
        int scaledHeight = (int) Math.max(1, Math.round(texture.height() * iconScale));
        double baseCenterX = widget.drawX() + texture.width() / 2.0;
        double baseCenterY = widget.drawY() + texture.height() / 2.0;
        double shiftedX = baseCenterX + translateX;
        double shiftedY = baseCenterY + translateY;
        double dx = shiftedX - centerX;
        double dy = shiftedY - centerY;
        double rotatedX = centerX + dx * cos - dy * sin;
        double rotatedY = centerY + dx * sin + dy * cos;
        int drawX = (int) Math.round(rotatedX - scaledWidth / 2.0);
        int drawY = (int) Math.round(rotatedY - scaledHeight / 2.0);
        this.drawFeatureIcon(guiGraphics, texture, drawX, drawY, scaledWidth, scaledHeight, colour);
    }

    private void drawCenterCross(GuiGraphics guiGraphics, double centerX, double centerY) {
        int cx = (int) Math.round(centerX);
        int cy = (int) Math.round(centerY);
        int crossHalf = 3;
        int color = 0xFF_FFFFFF;
        guiGraphics.fill(cx - crossHalf, cy, cx + crossHalf + 1, cy + 1, color);
        guiGraphics.fill(cx, cy - crossHalf, cx + 1, cy + crossHalf + 1, color);
    }

    public void focusOn(BlockPos pos) {
        this.updatePlayerPosition(pos);
        this.moveCenter(QuartPos2f.fromQuartPos(QuartPos2.fromBlockPos(pos)));
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    @Override
    protected void applyDefaultZoom() {
        this.setPixelsPerBiome(this.readPixelsPerBiomeFromConfig());
    }

    private void refreshPixelsPerBiome() {
        double configured = this.readPixelsPerBiomeFromConfig();
        if (Math.abs(configured - this.getPixelsPerBiome()) > 1.0E-4D) {
            this.setPixelsPerBiome(configured);
        }
    }

    @Override
    protected double readPixelsPerBiomeFromConfig() {
        return Configs.SeedMapMinimapPixelsPerBiome;
    }

    @Override
    protected void writePixelsPerBiomeToConfig(double pixelsPerBiome) {
        Configs.SeedMapMinimapPixelsPerBiome = pixelsPerBiome;
    }

    @Override
    protected boolean shouldRenderChestLootWidget() {
        return false;
    }

    @Override
    protected int getMapBackgroundTint() {
        float opacity = (float) Mth.clamp(Configs.SeedMapMinimapOpacity, 0.0D, 1.0D);
        int alpha = (int) Math.round(opacity * 255.0F);
        return (alpha << 24) | 0x00_FFFFFF;
    }

    @Override
    protected boolean showCoordinateOverlay() {
        return false;
    }

    @Override
    protected boolean showFeatureToggleTooltips() {
        return false;
    }

    @Override
    protected boolean showSeedLabel() {
        return false;
    }
}
