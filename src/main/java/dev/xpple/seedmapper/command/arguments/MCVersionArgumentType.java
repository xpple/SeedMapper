package dev.xpple.seedmapper.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.seedfinding.mccore.version.MCVersion;
import dev.xpple.seedmapper.command.SharedHelpers;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.TranslatableText;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class MCVersionArgumentType implements ArgumentType<MCVersion>, SharedHelpers.Exceptions {

    private static final Collection<String> EXAMPLES = Arrays.asList("1.17.1", "1.15", "b1.6.4");

    private static final SimpleCommandExceptionType DISALLOWED_VERSION_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.exceptions.disallowedVersion"));

    private MCVersion equalOrNewerThan = MCVersion.oldest();
    private boolean releaseOnly = true;

    public static MCVersionArgumentType mcVersion() {
        return new MCVersionArgumentType();
    }

    public static MCVersion getMcVersion(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, MCVersion.class);
    }

    public MCVersionArgumentType all() {
        this.releaseOnly = false;
        return this;
    }

    public MCVersionArgumentType equalOrNewerThan(MCVersion mcVersion) {
        this.equalOrNewerThan = mcVersion;
        return this;
    }

    @Override
    public MCVersion parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String versionString = reader.readUnquotedString();
        MCVersion mcVersion = MCVersion.fromString(versionString);
        if (mcVersion == null) {
            reader.setCursor(cursor);
            throw VERSION_NOT_FOUND_EXCEPTION.create(versionString);
        }
        if (this.releaseOnly && mcVersion.isOlderThan(MCVersion.v1_0)) {
            throw DISALLOWED_VERSION_EXCEPTION.create();
        }
        if (mcVersion.isOlderThan(this.equalOrNewerThan)) {
            throw DISALLOWED_VERSION_EXCEPTION.create();
        }
        return mcVersion;
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(CommandContext context, SuggestionsBuilder builder) {
        Stream<String> versions = Arrays.stream(MCVersion.values())
                .filter(mcVersion -> !this.releaseOnly || mcVersion.isNewerOrEqualTo(MCVersion.v1_0))
                .filter(mcVersion -> mcVersion.isNewerOrEqualTo(this.equalOrNewerThan))
                .map(mcVersion -> mcVersion.name);
        return CommandSource.suggestMatching(versions, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
