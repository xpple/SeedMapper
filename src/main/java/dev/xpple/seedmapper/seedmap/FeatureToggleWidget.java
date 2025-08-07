package dev.xpple.seedmapper.seedmap;

import dev.xpple.seedmapper.config.Configs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public class FeatureToggleWidget extends Button {

    private final MapFeature feature;

    public FeatureToggleWidget(MapFeature feature, int x, int y) {
        super(x, y, feature.getTexture().width(), feature.getTexture().height(), Component.literal(feature.getName()), Button::onPress, DEFAULT_NARRATION);
        this.feature = feature;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int colour = 0xff_ffffff;
        if (!Configs.ToggledFeatures.contains(this.feature)) {
            colour = ARGB.color(255 >> 1, 255, 255, 255);
        }
        this.drawFeatureIcon(guiGraphics, this.getX(), this.getY(), colour);
    }

    @Override
    public void onPress() {
        if (!Configs.ToggledFeatures.remove(this.feature)) {
            Configs.ToggledFeatures.add(this.feature);
        }
    }

    private void drawFeatureIcon(GuiGraphics guiGraphics, int minX, int minY, int colour) {
        int iconWidth = this.feature.getTexture().width();
        int iconHeight = this.feature.getTexture().height();
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.feature.getTexture().resourceLocation(), minX, minY, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight, iconWidth, iconHeight, colour);
    }
}
