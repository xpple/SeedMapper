package dev.xpple.seedmapper.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.version.MCVersion;
import dev.xpple.seedmapper.util.DatabaseHelper;
import dev.xpple.seedmapper.util.config.Configs;
import dev.xpple.seedmapper.util.config.SeedResolution;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.SharedConstants;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import static dev.xpple.seedmapper.SeedMapper.CLIENT;

public record SharedHelpers(long seed, Dimension dimension, MCVersion mcVersion) {

    public interface Exceptions {
        DynamicCommandExceptionType NULL_POINTER_EXCEPTION = new DynamicCommandExceptionType(arg -> Text.translatable("commands.exceptions.nullPointerException", arg));
        DynamicCommandExceptionType DIMENSION_NOT_SUPPORTED_EXCEPTION = new DynamicCommandExceptionType(arg -> Text.translatable("commands.exceptions.dimensionNotSupported", arg));
        DynamicCommandExceptionType VERSION_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> Text.translatable("commands.exceptions.versionNotFound", arg));
        SimpleCommandExceptionType UNSUPPORTED_VERSION_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.exceptions.unsupportedVersion"));
        SimpleCommandExceptionType INVALID_DIMENSION_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.exceptions.invalidDimension"));
        DynamicCommandExceptionType BIOME_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> Text.translatable("commands.exceptions.biomeNotFound", arg));
        DynamicCommandExceptionType STRUCTURE_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> Text.translatable("commands.exceptions.structureNotFound", arg));
        DynamicCommandExceptionType DECORATOR_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> Text.translatable("commands.exceptions.decoratorNotFound", arg));
        DynamicCommandExceptionType ITEM_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> Text.translatable("commands.exceptions.itemNotFound", arg));
        DynamicCommandExceptionType LOOT_ITEM_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> Text.translatable("commands.exceptions.lootItemNotFound", arg));
        DynamicCommandExceptionType BLOCK_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> Text.translatable("commands.exceptions.blockNotFound", arg));
        DynamicCommandExceptionType WORLD_SIMULATION_ERROR_EXCEPTION = new DynamicCommandExceptionType(arg -> Text.translatable("commands.exceptions.worldSimulationError", arg));
        SimpleCommandExceptionType REQUIRES_WORLD_SIMULATION_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.exceptions.requiresWorldSimulation"));
        SimpleCommandExceptionType UNSUPPORTED_BY_WORLD_SIMULATION_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.exceptions.unsupportedByWorldSimulation"));
    }

    public SharedHelpers(FabricClientCommandSource source) throws CommandSyntaxException {
        this(
            getSeed(source),
            source.getMeta("dimension") == null ?
                getDimension(source.getWorld().getRegistryKey().getValue().getPath()) :
                getDimension(((Identifier) source.getMeta("dimension")).getPath()),
            source.getMeta("version") == null ?
                getMCVersion(SharedConstants.getGameVersion().getName()) :
                (MCVersion) source.getMeta("version")
        );
    }

    public static long getSeed(@Nullable FabricClientCommandSource source) throws CommandSyntaxException {
        Long seed;
        for (SeedResolution.Method method : Configs.SeedResolutionOrder) {
            seed = switch (method) {
                case COMMAND_SOURCE -> source == null ? null : (Long) source.getMeta("seed");
                case SAVED_SEEDS_CONFIG -> {
                    String key = CLIENT.getNetworkHandler().getConnection().getAddress().toString();
                    yield Configs.SavedSeeds.get(key);
                }
                case ONLINE_DATABASE -> {
                    String key = CLIENT.getNetworkHandler().getConnection().getAddress().toString();
                    yield DatabaseHelper.getSeed(key);
                }
                case SEED_CONFIG -> Configs.Seed;
            };
            if (seed != null) {
                return seed;
            }
        }
        throw Exceptions.NULL_POINTER_EXCEPTION.create("Seed");
    }

    public static Dimension getDimension(String dimensionPath) throws CommandSyntaxException {
        Dimension dimension = Dimension.fromString(dimensionPath);
        if (dimension == null) {
            throw Exceptions.DIMENSION_NOT_SUPPORTED_EXCEPTION.create(dimensionPath);
        }
        return dimension;
    }

    public static MCVersion getMCVersion(String version) throws CommandSyntaxException {
        MCVersion mcVersion = MCVersion.fromString(version);
        if (mcVersion == null) {
            throw Exceptions.VERSION_NOT_FOUND_EXCEPTION.create(version);
        }
        return mcVersion;
    }

    public static BPos fromBlockPos(BlockPos pos) {
        return new BPos(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos fromBPos(BPos pos) {
        return new BlockPos(pos.getX(), pos.getY(), pos.getZ());
    }
}
