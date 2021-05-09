package dev.xpple.seedmapper;

import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.ClientCommandManager;
import dev.xpple.seedmapper.command.commands.ConfigCommand;
import dev.xpple.seedmapper.command.commands.SeedOverlayCommand;
import dev.xpple.seedmapper.command.commands.TerrainVersionCommand;
import dev.xpple.seedmapper.util.config.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.ServerCommandSource;

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

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        ClientCommandManager.clearClientSideCommands();

        ClientCommand.instantiate(new SeedOverlayCommand(), dispatcher);
        ClientCommand.instantiate(new TerrainVersionCommand(), dispatcher);
        ClientCommand.instantiate(new ConfigCommand(), dispatcher);
    }
}
