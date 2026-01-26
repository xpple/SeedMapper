package dev.xpple.seedmapper.seedmap;

import dev.xpple.seedmapper.config.Configs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public class FeatureToggleWidget extends Button {

    private final MapFeature feature;

    public FeatureToggleWidget(MapFeature feature, int x, int y, double scale) {
        super(x, y, (int)Math.floor(scale * feature.getDefaultTexture().width()), (int)Math.floor(scale * feature.getDefaultTexture().height()), Component.literal(feature.getName()), FeatureToggleWidget::onButtonPress, DEFAULT_NARRATION);
        this.feature = feature;
        this.setTooltip(Tooltip.create(Component.literal(this.feature.getName())));
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int colour = 0xff_ffffff;
        if (!Configs.ToggledFeatures.contains(this.feature)) {
            colour = ARGB.color(255 >> 1, 255, 255, 255);
        }
        SeedMapScreen.drawIconStatic(guiGraphics, this.feature.getDefaultTexture().identifier(), this.getX(), this.getY(), this.getWidth(), this.getHeight(), colour);
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
