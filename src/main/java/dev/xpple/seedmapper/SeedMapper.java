package dev.xpple.seedmapper;

import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.betterconfig.api.ModConfigBuilder;
import dev.xpple.seedmapper.command.arguments.BlockArgument;
import dev.xpple.seedmapper.command.arguments.SeedResolutionArgument;
import dev.xpple.seedmapper.command.commands.CheckSeedCommand;
import dev.xpple.seedmapper.command.commands.ClearScreenCommand;
import dev.xpple.seedmapper.command.commands.HighlightCommand;
import dev.xpple.seedmapper.command.commands.LocateCommand;
import dev.xpple.seedmapper.command.commands.SeedOverlayCommand;
import dev.xpple.seedmapper.command.commands.SourceCommand;
import dev.xpple.seedmapper.command.commands.TerrainVersionCommand;
import dev.xpple.seedmapper.util.SeedDatabaseHelper;
import dev.xpple.seedmapper.util.config.BlockAdapter;
import dev.xpple.seedmapper.util.config.Configs;
import dev.xpple.seedmapper.util.config.SeedResolutionAdapter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.world.level.block.Block;

import java.nio.file.Path;

public class SeedMapper implements ClientModInitializer {

    public static final String MOD_ID = "seedmapper";

    public static final Path modConfigPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);

    @Override
    public void onInitializeClient() {
        new ModConfigBuilder<>(MOD_ID, Configs.class)
            .registerTypeHierarchy(Block.class, new BlockAdapter(), BlockArgument::block)
            .registerType(SeedResolutionArgument.SeedResolution.class, new SeedResolutionAdapter(), SeedResolutionArgument::seedResolution)
            .build();

        SeedDatabaseHelper.fetchSeeds();

        ClientCommandRegistrationCallback.EVENT.register(SeedMapper::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context) {
        SeedOverlayCommand.register(dispatcher);
        TerrainVersionCommand.register(dispatcher);
        LocateCommand.register(dispatcher);
        HighlightCommand.register(dispatcher);
        SourceCommand.register(dispatcher);
        ClearScreenCommand.register(dispatcher);
        CheckSeedCommand.register(dispatcher);
    }
}
