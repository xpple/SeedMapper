package dev.xpple.seedmapper.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.xpple.seedmapper.util.config.Config;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.version.MCVersion;
import net.minecraft.text.TranslatableText;

import static dev.xpple.seedmapper.SeedMapper.CLIENT;

public final class SharedHelpers {

    public interface Exceptions {
        DynamicCommandExceptionType NULL_POINTER_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.nullPointerException", arg));
        DynamicCommandExceptionType DIMENSION_NOT_SUPPORTED_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.dimensionNotSupported", arg));
        DynamicCommandExceptionType VERSION_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.versionNotFound", arg));
        SimpleCommandExceptionType INVALID_DIMENSION_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.exceptions.invalidDimension"));
        DynamicCommandExceptionType BIOME_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.biomeNotFound", arg));
        DynamicCommandExceptionType STRUCTURE_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.structureNotFound", arg));
        DynamicCommandExceptionType LOOT_ITEM_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.lootItemNotFound", arg));
        DynamicCommandExceptionType BLOCK_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.blockNotFound", arg));
    }

    public static long getSeed() throws CommandSyntaxException {
        long seed;
        String key = CLIENT.getNetworkHandler().getConnection().getAddress().toString();
        if (Config.getSeeds().containsKey(key)) {
            seed = Config.getSeeds().get(key);
        } else {
            JsonElement element = Config.get("seed");
            if (element instanceof JsonNull) {
                throw Exceptions.NULL_POINTER_EXCEPTION.create("seed");
            }
            seed = element.getAsLong();
        }
        return seed;
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
}
