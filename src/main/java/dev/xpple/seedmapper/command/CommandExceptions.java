package dev.xpple.seedmapper.command;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.xpple.seedmapper.thread.LocatorThreadHelper;
import net.minecraft.network.chat.Component;

public final class CommandExceptions {

    private CommandExceptions() {
    }

    public static final SimpleCommandExceptionType ALREADY_BUSY_LOCATING_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.exceptions.alreadyBusyLocating", LocatorThreadHelper.STOP_TASK_COMPONENT));
    public static final DynamicCommandExceptionType UNKNOWN_DIMENSION_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownDimension", arg));
    public static final DynamicCommandExceptionType UNKNOWN_GENERATOR_FLAG_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownGeneratorFlag", arg));
    public static final DynamicCommandExceptionType UNKNOWN_VERSION_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownVersion", arg));
    public static final DynamicCommandExceptionType UNKNOWN_BIOME_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownBiome", arg));
    public static final DynamicCommandExceptionType UNKNOWN_STRUCTURE_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownStructure", arg));
    public static final DynamicCommandExceptionType UNKNOWN_STRUCTURE_PIECE_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownStructurePiece", arg));
    public static final DynamicCommandExceptionType UNKNOWN_VARIANT_KEY_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownVariantKey", arg));
    public static final DynamicCommandExceptionType UNKNOWN_VARIANT_VALUE_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownVariantValue", arg));
    public static final DynamicCommandExceptionType UNKNOWN_BLOCK_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownOre", arg));
    public static final DynamicCommandExceptionType UNKNOWN_ITEM_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownItem", arg));
    public static final DynamicCommandExceptionType UNKNOWN_ENCHANTMENT_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownEnchantment", arg));
    public static final DynamicCommandExceptionType UNKNOWN_CANYON_CARVER_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownCanyonCarver", arg));
    public static final DynamicCommandExceptionType UNKNOWN_CAVE_CARVER_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownCaveCarver", arg));
    public static final DynamicCommandExceptionType UNKNOWN_DENSITY_FUNCTION_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownDensityFunction", arg));
    public static final DynamicCommandExceptionType UNKNOWN_MAP_FEATURE_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownMapFeature", arg));
    public static final SimpleCommandExceptionType NO_SEED_AVAILABLE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.exceptions.noSeedAvailable"));
    public static final DynamicCommandExceptionType NO_BIOME_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.noBiomeFound", arg));
    public static final DynamicCommandExceptionType NO_STRUCTURE_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.noStructureFound", arg));
    public static final DynamicCommandExceptionType NO_ORE_VEIN_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.noOreVeinFound", arg));
    public static final DynamicCommandExceptionType NO_CANYON_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.noCanyonFound", arg));
    public static final SimpleCommandExceptionType INVALID_DIMENSION_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.exceptions.invalidDimension"));
    public static final SimpleCommandExceptionType INCOMPATIBLE_PARAMETERS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.exceptions.incompatibleParameters"));
    public static final SimpleCommandExceptionType BIOME_WRONG_VERSION_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.exceptions.biomeWrongVersion"));
    public static final SimpleCommandExceptionType ORE_VEIN_WRONG_VERSION_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.exceptions.oreVeinWrongVersion"));
    public static final SimpleCommandExceptionType CANYON_WRONG_VERSION_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.exceptions.canyonWrongVersion"));
    public static final SimpleCommandExceptionType CAVE_WRONG_VERSION_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.exceptions.caveWrongVersion"));
    public static final SimpleCommandExceptionType LOOT_NOT_SUPPORTED_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.exceptions.lootNotSupported"));
    public static final SimpleCommandExceptionType LOOT_NOT_AVAILABLE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.exceptions.lootNotAvailable"));
}
