package dev.xpple.seedmapper.command;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.xpple.seedmapper.command.commands.LocateCommand;
import net.minecraft.network.chat.Component;

public final class CommandExceptions {

    private CommandExceptions() {
    }

    public static final SimpleCommandExceptionType ALREADY_BUSY_LOCATING_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.exceptions.alreadyBusyLocating", LocateCommand.STOP_TASK_COMPONENT));
    public static final DynamicCommandExceptionType UNKNOWN_DIMENSION_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownDimension", arg));
    public static final DynamicCommandExceptionType UNKNOWN_VERSION_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownVersion", arg));
    public static final DynamicCommandExceptionType UNKNOWN_BIOME_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownBiome", arg));
    public static final DynamicCommandExceptionType UNKNOWN_STRUCTURE_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownStructure", arg));
    public static final DynamicCommandExceptionType UNKNOWN_STRUCTURE_PIECE_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownStructurePiece", arg));
    public static final DynamicCommandExceptionType UNKNOWN_VARIANT_KEY_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownVariantKey", arg));
    public static final DynamicCommandExceptionType UNKNOWN_VARIANT_VALUE_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownVariantValue", arg));
    public static final SimpleCommandExceptionType NO_SEED_AVAILABLE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.exceptions.noSeedAvailable"));
    public static final DynamicCommandExceptionType NO_BIOME_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.noBiomeFound", arg));
    public static final DynamicCommandExceptionType NO_STRUCTURE_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.noStructureFound", arg));
    public static final SimpleCommandExceptionType INVALID_DIMENSION_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.exceptions.invalidDimension"));
    public static final SimpleCommandExceptionType INCOMPATIBLE_PARAMETERS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.exceptions.incompatibleParameters"));
}
