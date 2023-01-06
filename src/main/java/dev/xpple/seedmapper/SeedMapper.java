package dev.xpple.seedmapper;

import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.commands.*;
import dev.xpple.seedmapper.util.config.Config;
import dev.xpple.seedmapper.util.DatabaseHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;

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
        DatabaseHelper.fetchSeeds();

        ClientCommandRegistrationCallback.EVENT.register(SeedMapper::registerCommands);
    }

    public static void onTerminateClient() {
        //
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        ClientCommand.instantiate(new SeedOverlayCommand(), dispatcher);
        ClientCommand.instantiate(new TerrainVersionCommand(), dispatcher);
        ClientCommand.instantiate(new ConfigCommand(), dispatcher);
        ClientCommand.instantiate(new LocateCommand(), dispatcher);
        ClientCommand.instantiate(new HighlightCommand(), dispatcher);
        ClientCommand.instantiate(new SourceCommand(), dispatcher);
        ClientCommand.instantiate(new ClearScreenCommand(), dispatcher);
        ClientCommand.instantiate(new CheckSeedCommand(), dispatcher);
    }
}
