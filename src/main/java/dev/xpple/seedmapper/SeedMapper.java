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
import net.minecraft.commands.CommandBuildContext;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class SeedMapper implements ClientModInitializer {

    public static final String MOD_ID = "seedmapper";

    public static final Path modConfigPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);

    static {
        String libraryName = System.mapLibraryName("libcubiomes");
        String extension = FilenameUtils.getExtension(libraryName);
        libraryName = "libcubiomes" + '.' + extension;
        URL libraryPath = SeedMapper.class.getClassLoader().getResource(libraryName);
        Path libcubiomes;
        try {
            libcubiomes = Files.createTempFile("libcubiomes", '.' + extension);
            IOUtils.copy(libraryPath, libcubiomes.toFile());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.load(libcubiomes.toAbsolutePath().toString());
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
