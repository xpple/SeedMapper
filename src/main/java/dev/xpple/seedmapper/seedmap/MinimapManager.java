package dev.xpple.seedmapper.seedmap;

import dev.xpple.seedmapper.SeedMapper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public final class MinimapManager {
    private MinimapManager() {
    }

    private static @Nullable MinimapScreen minimapScreen;

    public static void registerHudElement() {
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(SeedMapper.MOD_ID, "minimap"), MinimapManager::render);
    }

    public static boolean isVisible() {
        return minimapScreen != null;
    }

    public static void show(long seed, int dimension, int version, int generatorFlags) {
        hide();
        minimapScreen = new MinimapScreen(seed, dimension, version, generatorFlags);
    }

    public static void hide() {
        if (minimapScreen != null) {
            if (minimapScreen.isInitialized()) {
                minimapScreen.onClose();
            }
            minimapScreen = null;
        }
    }

    public static void updateDimension(int dimension) {
        if (minimapScreen == null) {
            return;
        }
        if (minimapScreen.getDimension() == dimension) {
            return;
        }
        show(minimapScreen.getSeed(), dimension, minimapScreen.getVersion(), minimapScreen.getGeneratorFlags());
    }

    private static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (minimapScreen == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        minimapScreen.update(player.blockPosition(), player.getRotationVector());
        minimapScreen.renderToHud(guiGraphics, deltaTracker.getGameTimeDeltaTicks());
    }
}
