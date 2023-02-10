package dev.xpple.seedmapper.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.xpple.seedmapper.util.config.SeedResolution;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SeedResolutionArgumentType implements ArgumentType<SeedResolution> {

    private static final Collection<String> EXAMPLES = Arrays.asList("CommandSource SavedSeedsConfig OnlineDatabase SeedConfig", "CommandSource SeedConfig");

    private static final DynamicCommandExceptionType UNKNOWN_RESOLUTION_METHOD_EXCEPTION = new DynamicCommandExceptionType(arg -> Text.translatable("commands.exceptions.unknownResolutionMethod", arg));

    public static SeedResolutionArgumentType seedResolution() {
        return new SeedResolutionArgumentType();
    }

    public static SeedResolution getSeedResolution(CommandContext<? extends CommandSource> context, String name) {
        return context.getArgument(name, SeedResolution.class);
    }

    @Override
    public SeedResolution parse(StringReader reader) throws CommandSyntaxException {
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

        if (parser.suggestor != null) {
            parser.suggestor.accept(builder);
        }

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static class Parser {

        private final StringReader reader;
        private Consumer<SuggestionsBuilder> suggestor;

        private final List<SeedResolution.Method> methods = new ArrayList<>(Arrays.asList(SeedResolution.Method.values()));
        private final List<SeedResolution.Method> order = new ArrayList<>();

        private Parser(StringReader reader) {
            this.reader = reader;
        }

        private SeedResolution parse() throws CommandSyntaxException {
            while (!this.methods.isEmpty()) {
                int cursor = this.reader.getCursor();
                this.suggestor = builder -> {
                    SuggestionsBuilder newBuilder = builder.createOffset(cursor);
                    CommandSource.suggestMatching(this.methods.stream().map(SeedResolution.Method::asString), newBuilder);
                    builder.add(newBuilder);
                };
                if (!this.reader.canRead()) {
                    break;
                }
                String methodString = this.reader.readUnquotedString();
                SeedResolution.Method method = SeedResolution.Method.CODEC.byId(methodString);
                if (method == null) {
                    throw UNKNOWN_RESOLUTION_METHOD_EXCEPTION.create(methodString);
                }
                if (this.order.contains(method)) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
                }
                this.order.add(method);
                this.methods.remove(method);

                if (this.reader.canRead()) {
                    this.reader.expect(' ');
                } else {
                    break;
                }
            }
            this.order.addAll(this.methods);
            return new SeedResolution(this.order);
        }
    }
}
