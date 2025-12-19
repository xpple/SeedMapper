package dev.xpple.seedmapper.command.arguments;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.xpple.betterconfig.util.CheckedFunction;
import dev.xpple.seedmapper.util.SeedIdentifier;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SeedIdentifierArgument implements ArgumentType<SeedIdentifier> {

    private static final Collection<String> EXAMPLES = List.of("8052710360952744907", "-123 --version 1.17.1", "-123 --generatorFlags large_biomes --version 1.21.11");

    private static final DynamicCommandExceptionType UNKNOWN_SEED_ARGUMENT_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownSeedArgument", arg));

    public static SeedIdentifierArgument seedIdentifier() {
        return new SeedIdentifierArgument();
    }

    public SeedIdentifier getSeedIdentifier(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, SeedIdentifier.class);
    }

    @Override
    public SeedIdentifier parse(StringReader reader) throws CommandSyntaxException {
        return new Parser(reader).parse();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader reader = new StringReader(builder.getInput());
        reader.setCursor(builder.getStart());

        Parser parser = new Parser(reader);

        try {
            parser.parse();
        } catch (CommandSyntaxException ignored) {
        }

        if (parser.suggester != null) {
            parser.suggester.accept(builder);
        }

        return builder.buildFuture();
    }

    private static class Parser {
        private final StringReader reader;
        private Consumer<SuggestionsBuilder> suggester;

        private final Map<String, CheckedFunction<SeedIdentifier, SeedIdentifier, CommandSyntaxException>> ARGUMENTS = ImmutableMap.<String, CheckedFunction<SeedIdentifier, SeedIdentifier, CommandSyntaxException>>builder()
            .put("--version", this::parseVersion)
            .put("--generatorFlag", this::parseGeneratorFlag)
            .build();

        private Parser(StringReader reader) {
            this.reader = reader;
        }

        private SeedIdentifier parse() throws CommandSyntaxException {
            long seed = LongArgumentType.longArg().parse(this.reader);
            SeedIdentifier identifier = new SeedIdentifier(seed);

            if (!this.reader.canRead()) {
                return identifier;
            }

            this.reader.expect(' ');
            this.reader.skipWhitespace();

            Set<String> remainingArguments = new HashSet<>(ARGUMENTS.keySet());
            while (!remainingArguments.isEmpty()) {
                int cursor = this.reader.getCursor();
                this.suggester = suggestions -> {
                    SuggestionsBuilder builder = suggestions.createOffset(cursor);
                    SharedSuggestionProvider.suggest(remainingArguments, builder);
                    suggestions.add(builder);
                };
                if (!this.reader.canRead()) {
                    break;
                }
                String argumentString = this.reader.readUnquotedString();
                CheckedFunction<SeedIdentifier, SeedIdentifier, CommandSyntaxException> parser = ARGUMENTS.get(argumentString);
                if (parser == null) {
                    this.reader.setCursor(cursor);
                    throw UNKNOWN_SEED_ARGUMENT_EXCEPTION.create(argumentString);
                }
                this.reader.expect(' ');
                this.reader.skipWhitespace();
                identifier = parser.apply(identifier);
                if (!argumentString.equals("--generatorFlag")) { // allow multiple flags
                    remainingArguments.remove(argumentString);
                }
                if (this.reader.canRead()) {
                    this.reader.expect(' ');
                    this.reader.skipWhitespace();
                } else {
                    break;
                }
            }
            return identifier;
        }

        private SeedIdentifier parseVersion(SeedIdentifier seed) throws CommandSyntaxException {
            int cursor = this.reader.getCursor();
            this.suggester = builder -> {
                SuggestionsBuilder newBuilder = builder.createOffset(cursor);
                SharedSuggestionProvider.suggest(VersionArgument.VERSIONS.keySet(), newBuilder);
                builder.add(newBuilder);
            };
            int version = VersionArgument.version().parse(this.reader);
            return seed.withVersion(version);
        }

        private SeedIdentifier parseGeneratorFlag(SeedIdentifier identifier) throws CommandSyntaxException {
            int cursor = this.reader.getCursor();
            this.suggester = builder -> {
                SuggestionsBuilder newBuilder = builder.createOffset(cursor);
                SharedSuggestionProvider.suggest(GeneratorFlagArgument.GENERATOR_FLAGS.keySet(), newBuilder);
                builder.add(newBuilder);
            };
            int generatorFlag = GeneratorFlagArgument.generatorFlag().parse(this.reader);
            return identifier.withGeneratorFlag(generatorFlag);
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
