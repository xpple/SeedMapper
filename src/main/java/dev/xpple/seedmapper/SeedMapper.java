package dev.xpple.seedmapper;

import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.commands.*;
import dev.xpple.seedmapper.util.config.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SeedMapper implements ClientModInitializer {

    public static final String MOD_ID = "seedmapper";
    public static final String MOD_NAME = "SeedMapper";
    public static final Path MOD_PATH = Paths.get(FabricLoader.getInstance().getConfigDir() + File.separator + MOD_ID);
    public static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    @Override
    public void onInitializeClient() {
        //noinspection ResultOfMethodCallIgnored
        MOD_PATH.toFile().mkdirs();

        Config.init();
    }

    public static void onTerminateClient() {
        //
    }

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        ClientCommand.instantiate(new SeedOverlayCommand(), dispatcher);
        ClientCommand.instantiate(new TerrainVersionCommand(), dispatcher);
        ClientCommand.instantiate(new ConfigCommand(), dispatcher);
        ClientCommand.instantiate(new LocateCommand(), dispatcher);
        ClientCommand.instantiate(new HighlightCommand(), dispatcher);
        ClientCommand.instantiate(new SourceCommand(), dispatcher);
        ClientCommand.instantiate(new ClearScreenCommand(), dispatcher);
    }
}
