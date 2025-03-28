package dev.xpple.seedmapper;

import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.betterconfig.api.ModConfigBuilder;
import dev.xpple.seedmapper.command.arguments.SeedResolutionArgument;
import dev.xpple.seedmapper.command.commands.CheckSeedCommand;
import dev.xpple.seedmapper.command.commands.LocateCommand;
import dev.xpple.seedmapper.command.commands.SourceCommand;
import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.config.SeedResolutionAdapter;
import dev.xpple.seedmapper.util.SeedDatabaseHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.commands.CommandBuildContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SeedMapper implements ClientModInitializer {

    public static final String MOD_ID = "seedmapper";

    public static final Path modConfigPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);

    static {
        String libraryName = System.mapLibraryName("cubiomes");
        ModContainer modContainer = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow();
        try (BufferedReader locationReader = Files.newBufferedReader(modContainer.findPath("library_location.txt").orElseThrow());
             OutputStream outputStream = Files.newOutputStream(Paths.get(locationReader.readLine()))
        ) {
            Files.copy(modContainer.findPath(libraryName).orElseThrow(), outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onInitializeClient() {
        new ModConfigBuilder<>(MOD_ID, Configs.class)
            .registerType(SeedResolutionArgument.SeedResolution.class, new SeedResolutionAdapter(), SeedResolutionArgument::seedResolution)
            .build();

        SeedDatabaseHelper.fetchSeeds();

        ClientCommandRegistrationCallback.EVENT.register(SeedMapper::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context) {
        LocateCommand.register(dispatcher);
        SourceCommand.register(dispatcher);
        CheckSeedCommand.register(dispatcher);
    }
}
