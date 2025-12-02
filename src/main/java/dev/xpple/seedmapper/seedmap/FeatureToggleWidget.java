package dev.xpple.seedmapper.seedmap;

import dev.xpple.seedmapper.config.Configs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public class FeatureToggleWidget extends Button {

    private final MapFeature feature;

    public FeatureToggleWidget(MapFeature feature, int x, int y) {
        super(x, y, feature.getDefaultTexture().width(), feature.getDefaultTexture().height(), Component.literal(feature.getName()), FeatureToggleWidget::onButtonPress, DEFAULT_NARRATION);
        this.feature = feature;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int colour = 0xff_ffffff;
        if (!Configs.ToggledFeatures.contains(this.feature)) {
            colour = ARGB.color(255 >> 1, 255, 255, 255);
        }
        SeedMapScreen.FeatureWidget.drawFeatureIcon(guiGraphics, this.feature.getDefaultTexture(), this.getX(), this.getY(), colour);
    }

    private static void onButtonPress(Button button) {
        if (!(button instanceof FeatureToggleWidget widget)) {
            return;
        }
        if (!Configs.ToggledFeatures.remove(widget.feature)) {
            Configs.ToggledFeatures.add(widget.feature);
        }
    }
}
