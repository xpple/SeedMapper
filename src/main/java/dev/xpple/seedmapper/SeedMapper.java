package dev.xpple.seedmapper;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.betterconfig.api.ModConfigBuilder;
import dev.xpple.seedmapper.command.arguments.MapFeatureArgument;
import dev.xpple.seedmapper.command.arguments.SeedResolutionArgument;
import dev.xpple.seedmapper.command.commands.BuildInfoCommand;
import dev.xpple.seedmapper.command.commands.CheckSeedCommand;
import dev.xpple.seedmapper.command.commands.ClearCommand;
import dev.xpple.seedmapper.command.commands.DiscordCommand;
import dev.xpple.seedmapper.command.commands.HighlightCommand;
import dev.xpple.seedmapper.command.commands.LocateCommand;
import dev.xpple.seedmapper.command.commands.SeedMapCommand;
import dev.xpple.seedmapper.command.commands.SourceCommand;
import dev.xpple.seedmapper.command.commands.StopTaskCommand;
import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.config.MapFeatureAdapter;
import dev.xpple.seedmapper.config.SeedResolutionAdapter;
import dev.xpple.seedmapper.render.RenderManager;
import dev.xpple.seedmapper.seedmap.MapFeature;
import dev.xpple.seedmapper.util.SeedDatabaseHelper;
import dev.xpple.simplewaypoints.api.SimpleWaypointsAPI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandBuildContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class SeedMapper implements ClientModInitializer {

    public static final String MOD_ID = "seedmapper";

    public static final Path modConfigPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);

    static {
        String libraryName = System.mapLibraryName("cubiomes");
        ModContainer modContainer = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow();
        Path tempFile;
        try {
            tempFile = Files.createTempFile(libraryName, "");
            Files.copy(modContainer.findPath(libraryName).orElseThrow(), tempFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.load(tempFile.toAbsolutePath().toString());
    }

    @Override
    public void onInitializeClient() {
        new ModConfigBuilder<>(MOD_ID, Configs.class)
            .registerType(SeedResolutionArgument.SeedResolution.class, new SeedResolutionAdapter(), SeedResolutionArgument::seedResolution)
            .registerType(MapFeature.class, new MapFeatureAdapter(), MapFeatureArgument::mapFeature)
            .build();

        SimpleWaypointsAPI.getInstance().registerCommandAlias("sm:waypoint");

        SeedDatabaseHelper.fetchSeeds();

        KeyMapping keyMapping = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.seedMap", InputConstants.KEY_M, KeyMapping.Category.GAMEPLAY));
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            while (keyMapping.consumeClick()) {
                minecraft.player.connection.sendCommand("sm:seedmap");
            }
        });

        ClientCommandRegistrationCallback.EVENT.register(SeedMapper::registerCommands);
        RenderManager.registerEvents();
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context) {
        LocateCommand.register(dispatcher);
        SourceCommand.register(dispatcher);
        CheckSeedCommand.register(dispatcher);
        BuildInfoCommand.register(dispatcher);
        HighlightCommand.register(dispatcher);
        ClearCommand.register(dispatcher);
        StopTaskCommand.register(dispatcher);
        SeedMapCommand.register(dispatcher);
        DiscordCommand.register(dispatcher);
    }
}
