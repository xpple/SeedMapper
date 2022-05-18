package dev.xpple.seedmapper.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.version.MCVersion;
import dev.xpple.seedmapper.util.config.Config;
import dev.xpple.seedmapper.util.database.DatabaseHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import static dev.xpple.seedmapper.SeedMapper.CLIENT;

public final class SharedHelpers {

    public interface Exceptions {
        DynamicCommandExceptionType NULL_POINTER_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.nullPointerException", arg));
        DynamicCommandExceptionType DIMENSION_NOT_SUPPORTED_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.dimensionNotSupported", arg));
        DynamicCommandExceptionType VERSION_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.versionNotFound", arg));
        SimpleCommandExceptionType INVALID_DIMENSION_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.exceptions.invalidDimension"));
        DynamicCommandExceptionType BIOME_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.biomeNotFound", arg));
        DynamicCommandExceptionType STRUCTURE_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.structureNotFound", arg));
        DynamicCommandExceptionType DECORATOR_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.decoratorNotFound", arg));
        DynamicCommandExceptionType ITEM_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.itemNotFound", arg));
        DynamicCommandExceptionType LOOT_ITEM_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.lootItemNotFound", arg));
        DynamicCommandExceptionType BLOCK_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.blockNotFound", arg));
    }

    public long seed;
    public Dimension dimension;
    public MCVersion mcVersion;

    public SharedHelpers(FabricClientCommandSource source) throws CommandSyntaxException {
        this.seed = getSeed();
        if (source.getMeta("dimension") == null) {
            this.dimension = getDimension(source.getWorld().getRegistryKey().getValue().getPath());
        } else {
            this.dimension = getDimension(((Identifier) source.getMeta("dimension")).getPath());
        }
        if (source.getMeta("version") == null) {
            this.mcVersion = getMCVersion(CLIENT.getGame().getVersion().getName());
        } else {
            this.mcVersion = (MCVersion) source.getMeta("version");
        }
    }

    // TODO: 18/05/2022 await poll seed priority
    public static long getSeed() throws CommandSyntaxException {
        String key = CLIENT.getNetworkHandler().getConnection().getAddress().toString();
        Long seed = Config.getSeeds().get(key);
        if (seed != null) {
            return seed;
        }
        seed = DatabaseHelper.getSeed(key);
        if (seed != null) {
            return seed;
        }
        JsonElement element = Config.get("seed");
        if (element instanceof JsonNull) {
            throw Exceptions.NULL_POINTER_EXCEPTION.create("seed");
        }
        return element.getAsLong();
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
