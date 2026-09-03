package dev.xpple.seedmapper.seedmap;

import dev.xpple.seedmapper.SeedMapper;
import dev.xpple.seedmapper.util.SeedIdentifierWithDimension;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public final class MinimapManager {
    private MinimapManager() {
    }

    private static @Nullable Minimap minimap;

    public static void registerHudElement() {
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(SeedMapper.MOD_ID, "minimap"), MinimapManager::render);
    }

    public static boolean isVisible() {
        return minimap != null;
    }

    public static void show(SeedIdentifierWithDimension seedIdentifierWithDimension) {
        hide();
        minimap = new Minimap(seedIdentifierWithDimension);
    }

    public static void hide() {
        if (minimap != null) {
            if (minimap.isInitialized()) {
                minimap.close();
            }
            minimap = null;
        }
    }

    public static void updateDimension(int dimension) {
        if (minimap == null) {
            return;
        }
        if (minimap.getSeedMapRenderer().getSeedMapData().getSeedIdentifierWithDimension().dimension() == dimension) {
            return;
        }
        show(minimap.getSeedMapRenderer().getSeedMapData().getSeedIdentifierWithDimension().withDimension(dimension));
    }

    private static void render(GuiGraphicsExtractor guiGraphicsExtractor, DeltaTracker deltaTracker) {
        if (minimap == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        minimap.update(player.position(), player.getRotationVector());
        minimap.renderToHud(guiGraphicsExtractor, deltaTracker.getGameTimeDeltaTicks());
    }
}
