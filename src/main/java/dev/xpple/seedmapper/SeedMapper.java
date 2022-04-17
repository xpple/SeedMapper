package dev.xpple.seedmapper;

import dev.xpple.seedmapper.command.commands.*;
import dev.xpple.seedmapper.util.config.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SeedMapper implements ClientModInitializer {

    public static final String MOD_ID = "seedmapper";
    public static final String MOD_NAME = "SeedMapper";
    public static final String MOD_PREFIX = "sm";
    public static final Path MOD_PATH = Paths.get(FabricLoader.getInstance().getConfigDir() + File.separator + MOD_ID);
    public static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    @Override
    public void onInitializeClient() {
        //noinspection ResultOfMethodCallIgnored
        MOD_PATH.toFile().mkdirs();

        Config.init();

        registerCommands();
    }

    public static void onTerminateClient() {
        //
    }

    private static void registerCommands() {
        new SeedOverlayCommand().instantiate();
        new TerrainVersionCommand().instantiate();
        new ConfigCommand().instantiate();
        new LocateCommand().instantiate();
        new HighlightCommand().instantiate();
        new SourceCommand().instantiate();
        new ClearScreenCommand().instantiate();
    }
}
