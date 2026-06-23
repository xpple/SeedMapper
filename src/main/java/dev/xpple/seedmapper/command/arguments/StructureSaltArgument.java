package dev.xpple.seedmapper.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Pair;
import dev.xpple.seedmapper.command.CommandExceptions;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class StructureSaltArgument implements ArgumentType<Pair<Integer, Integer>> {

    private static final Collection<String> EXAMPLES = List.of("shipwreck=2048571934");

    public static StructureSaltArgument structureSalt() {
        return new StructureSaltArgument();
    }

    @SuppressWarnings("unchecked")
    public static Pair<Integer, Integer> getStructureSalt(CommandContext<FabricClientCommandSource> context, String name) {
        return (Pair<Integer, Integer>) context.getArgument(name, Pair.class);
    }

    @Override
    public Pair<Integer, Integer> parse(StringReader reader) throws CommandSyntaxException {
        return new Parser(reader).parse();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader reader = new StringReader(builder.getInput());
        reader.setCursor(builder.getStart());

        StructureSaltArgument.Parser parser = new StructureSaltArgument.Parser(reader);

        try {
            parser.parse();
        } catch (CommandSyntaxException ignored) {
        }

        if (parser.suggester != null) {
            parser.suggester.accept(builder);
        }

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static final class Parser {
        private final StringReader reader;
        private Consumer<SuggestionsBuilder> suggester;

        private Parser(StringReader reader) {
            this.reader = reader;
        }

        private Pair<Integer, Integer> parse() throws CommandSyntaxException {
            int cursor = this.reader.getCursor();
            this.suggester = builder -> {
                SuggestionsBuilder newBuilder = builder.createOffset(cursor);
                SharedSuggestionProvider.suggest(StructurePredicateArgument.STRUCTURES.keySet(), newBuilder);
                builder.add(newBuilder);
            };
            String structureString = this.reader.readUnquotedString();
            Integer structure = StructurePredicateArgument.STRUCTURES.get(structureString);
            if (structure == null) {
                this.reader.setCursor(cursor);
                throw CommandExceptions.UNKNOWN_STRUCTURE_EXCEPTION.create(structureString);
            }
            this.reader.expect('=');
            int structureSalt = this.reader.readInt();
            return Pair.of(structure, structureSalt);
        }
    }
}
