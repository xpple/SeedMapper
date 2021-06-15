package dev.xpple.seedmapper.command;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.text.TranslatableText;

public interface SharedExceptions {
    DynamicCommandExceptionType NULL_POINTER_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.nullPointerException", arg));
    DynamicCommandExceptionType DIMENSION_NOT_SUPPORTED_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.dimensionNotSupported", arg));
    DynamicCommandExceptionType VERSION_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.versionNotFound", arg));
    DynamicCommandExceptionType BIOME_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.biomeNotFound", arg));
    DynamicCommandExceptionType STRUCTURE_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.structureNotFound", arg));
}
