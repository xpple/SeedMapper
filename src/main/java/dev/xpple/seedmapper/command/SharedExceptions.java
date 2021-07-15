package dev.xpple.seedmapper.command;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.text.TranslatableText;

public interface SharedExceptions {
    DynamicCommandExceptionType NULL_POINTER_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.nullPointerException", arg));
    DynamicCommandExceptionType DIMENSION_NOT_SUPPORTED_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.dimensionNotSupported", arg));
    DynamicCommandExceptionType VERSION_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.versionNotFound", arg));
    SimpleCommandExceptionType INVALID_DIMENSION_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.exceptions.invalidDimension"));
    DynamicCommandExceptionType BIOME_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.biomeNotFound", arg));
    DynamicCommandExceptionType STRUCTURE_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.structureNotFound", arg));
    DynamicCommandExceptionType LOOT_ITEM_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.lootItemNotFound", arg));
}
