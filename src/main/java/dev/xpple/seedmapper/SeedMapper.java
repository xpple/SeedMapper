package dev.xpple.seedmapper;

import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.betterconfig.api.ModConfigBuilder;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.arguments.BlockArgumentType;
import dev.xpple.seedmapper.command.arguments.SeedResolutionArgumentType;
import dev.xpple.seedmapper.command.commands.*;
import dev.xpple.seedmapper.util.DatabaseHelper;
import dev.xpple.seedmapper.util.config.BlockAdapter;
import dev.xpple.seedmapper.util.config.Configs;
import dev.xpple.seedmapper.util.config.SeedResolution;
import dev.xpple.seedmapper.util.config.SeedResolutionAdapter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.util.Pair;

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

        new ModConfigBuilder(MOD_ID, Configs.class)
            .registerTypeHierarchyWithArgument(Block.class, new BlockAdapter(), new Pair<>(BlockArgumentType::block, BlockArgumentType::getBlock))
            .registerTypeWithArgument(SeedResolution.class, new SeedResolutionAdapter(), new Pair<>(SeedResolutionArgumentType::seedResolution, SeedResolutionArgumentType::getSeedResolution))
            .build();

        DatabaseHelper.fetchSeeds();

        ClientCommandRegistrationCallback.EVENT.register(SeedMapper::registerCommands);
    }

    public static void onTerminateClient() {
        //
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        ClientCommand.instantiate(new SeedOverlayCommand(), dispatcher, registryAccess);
        ClientCommand.instantiate(new TerrainVersionCommand(), dispatcher, registryAccess);
        ClientCommand.instantiate(new LocateCommand(), dispatcher, registryAccess);
        ClientCommand.instantiate(new HighlightCommand(), dispatcher, registryAccess);
        ClientCommand.instantiate(new SourceCommand(), dispatcher, registryAccess);
        ClientCommand.instantiate(new ClearScreenCommand(), dispatcher, registryAccess);
        ClientCommand.instantiate(new CheckSeedCommand(), dispatcher, registryAccess);
    }
}
