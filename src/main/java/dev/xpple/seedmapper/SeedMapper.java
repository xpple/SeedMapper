package dev.xpple.seedmapper;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.StructureConfig;
import com.github.cubiomes.StructureConfigProvider;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import dev.xpple.betterconfig.api.BetterConfigAPI;
import dev.xpple.betterconfig.api.ModConfigBuilder;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.command.arguments.BlockArgument;
import dev.xpple.seedmapper.command.arguments.ColorWrapperArgument;
import dev.xpple.seedmapper.command.arguments.DurationArgument;
import dev.xpple.seedmapper.command.arguments.MapFeatureArgument;
import dev.xpple.seedmapper.command.arguments.SeedIdentifierArgument;
import dev.xpple.seedmapper.command.arguments.SeedResolutionArgument;
import dev.xpple.seedmapper.command.arguments.StructurePredicateArgument;
import dev.xpple.seedmapper.command.commands.BuildInfoCommand;
import dev.xpple.seedmapper.command.commands.CheckSeedCommand;
import dev.xpple.seedmapper.command.commands.ClearCommand;
import dev.xpple.seedmapper.command.commands.DiscordCommand;
import dev.xpple.seedmapper.command.commands.FindCommand;
import dev.xpple.seedmapper.command.commands.HighlightCommand;
import dev.xpple.seedmapper.command.commands.LocateCommand;
import dev.xpple.seedmapper.command.commands.MinimapCommand;
import dev.xpple.seedmapper.command.commands.SampleCommand;
import dev.xpple.seedmapper.command.commands.SeedMapCommand;
import dev.xpple.seedmapper.command.commands.SourceCommand;
import dev.xpple.seedmapper.command.commands.StopTaskCommand;
import dev.xpple.seedmapper.command.commands.VaultCommand;
import dev.xpple.seedmapper.config.ColorWrapper;
import dev.xpple.seedmapper.config.ColorWrapperAdapter;
import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.config.DurationAdapter;
import dev.xpple.seedmapper.config.MapFeatureAdapter;
import dev.xpple.seedmapper.config.SeedIdentifierAdapter;
import dev.xpple.seedmapper.config.SeedResolutionAdapter;
import dev.xpple.seedmapper.render.RenderManager;
import dev.xpple.seedmapper.seedmap.MapFeature;
import dev.xpple.seedmapper.seedmap.MinimapManager;
import dev.xpple.seedmapper.util.SeedDatabaseHelper;
import dev.xpple.seedmapper.util.SeedIdentifier;
import dev.xpple.simplewaypoints.api.SimpleWaypointsAPI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

public class SeedMapper implements ClientModInitializer {

    public static final String MOD_ID = "seedmapper";
    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow();

    public static final Path modConfigPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final boolean BARITONE_AVAILABLE = FabricLoader.getInstance().getModContainer("baritone-meteor").isPresent();

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
            .registerType(SeedIdentifier.class, new SeedIdentifierAdapter(), SeedIdentifierArgument::seedIdentifier)
            .registerType(SeedResolutionArgument.SeedResolution.class, new SeedResolutionAdapter(), SeedResolutionArgument::seedResolution)
            .registerTypeHierarchy(MapFeature.class, new MapFeatureAdapter(), MapFeatureArgument::mapFeature)
            .registerType(Duration.class, new DurationAdapter(), DurationArgument::duration)
            .registerType(ColorWrapper.class, new ColorWrapperAdapter(), ColorWrapperArgument::colorWrapper)
            .registerGlobalChangeHook(event -> {
                if (event.config().equals("DevMode")) {
                    try {
                        ClientCommands.refreshCommandCompletions();
                    } catch (IllegalStateException _) {
                    }
                }
            })
            .registerCommandAlias("sm:config")
            .build();

        // FIXME remove migration code at some point
        Configs.BlockColors = Configs.BlockColors.entrySet().stream()
            .collect(Collectors.toMap(entry -> {
                String key = entry.getKey();
                int id;
                try {
                    id = Integer.parseInt(key);
                } catch (NumberFormatException _) {
                    return key;
                }
                return BlockArgument.BLOCKS.inverse().get(id);
            }, Map.Entry::getValue));
        BetterConfigAPI.getInstance().getModConfig(MOD_ID).save();

        Cubiomes.setStructureConfigProvider(StructureConfigProvider.allocate((stype, mc, sconf) -> {
            if (Cubiomes.getStructureConfig_default(stype, mc, sconf) == 0) {
                return 0;
            }
            CustomClientCommandSource commandSource = CustomClientCommandSource.makeFakeCommandSource();
            if (commandSource == null) {
                return 1;
            }
            Integer salt;
            try {
                String structureString = StructurePredicateArgument.STRUCTURES.inverse().get(stype);
                salt = commandSource.getSeed().getSecond().customStructureSalts().get(structureString);
            } catch (CommandSyntaxException _) {
                return 1;
            }
            if (salt != null) {
                StructureConfig.salt(sconf, salt);
            }
            return 1;
        }, Arena.global()));

        SimpleWaypointsAPI.getInstance().registerCommandAlias("sm:waypoint");

        SeedDatabaseHelper.fetchSeeds();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, MOD_ID));
        KeyMapping seedMapKeyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.seedMap", InputConstants.KEY_M, category));
        KeyMapping minimapKeyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.minimap", InputConstants.KEY_COMMA, category));
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            while (seedMapKeyMapping.consumeClick()) {
                minecraft.player.connection.sendCommand("sm:seedmap");
            }
            while (minimapKeyMapping.consumeClick()) {
                minecraft.player.connection.sendCommand("sm:minimap");
            }
        });

        ClientCommandRegistrationCallback.EVENT.register(SeedMapper::registerCommands);
        RenderManager.registerEvents();
        MinimapManager.registerHudElement();

        if (BARITONE_AVAILABLE) {
            LOGGER.info("Baritone detected, Baritone integration will be available!");
            LOGGER.info("Set AutoMine to true to automatically mine certain blocks highlighted by `/sm:highlight`");
        }
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
        SampleCommand.register(dispatcher);
        MinimapCommand.register(dispatcher);
        VaultCommand.register(dispatcher);
        FindCommand.register(dispatcher);
    }
}
