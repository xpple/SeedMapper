package dev.xpple.seedmapper.command.arguments;

import com.google.common.base.Joiner;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SeedResolutionArgument implements ArgumentType<SeedResolutionArgument.SeedResolution> {

    private static final Collection<String> EXAMPLES = Arrays.asList("CommandSource SavedSeedsConfig OnlineDatabase SeedConfig", "CommandSource SeedConfig");

    private static final DynamicCommandExceptionType UNKNOWN_RESOLUTION_METHOD_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.exceptions.unknownResolutionMethod", arg));

    public static SeedResolutionArgument seedResolution() {
        return new SeedResolutionArgument();
    }

    public static SeedResolution getSeedResolution(CommandContext<FabricClientCommandSource> context, String name) {
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
                    SharedSuggestionProvider.suggest(this.methods.stream().map(SeedResolution.Method::getSerializedName), newBuilder);
                    builder.add(newBuilder);
                };
                if (!this.reader.canRead()) {
                    break;
                }
                String methodString = this.reader.readUnquotedString();
                SeedResolution.Method method = SeedResolution.Method.CODEC.byName(methodString);
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

    public static class SeedResolution implements Iterable<SeedResolution.Method> {

        private final List<Method> methods;

        public SeedResolution() {
            this.methods = List.of(Method.values());
        }

        public SeedResolution(List<Method> methods) {
            this.methods = methods;
        }

        @NotNull
        @Override
        public Iterator<Method> iterator() {
            return this.methods.iterator();
        }

        public enum Method implements StringRepresentable {

            COMMAND_SOURCE("CommandSource"),
            SEED_CONFIG("SeedConfig"),
            SAVED_SEEDS_CONFIG("SavedSeedsConfig"),
            ONLINE_DATABASE("OnlineDatabase");

            public static final StringRepresentable.EnumCodec<Method> CODEC = StringRepresentable.fromEnum(Method::values);

            private final String name;

            Method(String name) {
                this.name = name;
            }

            @Override
            public @NotNull String getSerializedName() {
                return this.name;
            }

            @Override
            public String toString() {
                return this.getSerializedName();
            }
        }

        @Override
        public String toString() {
            return Joiner.on(" -> ").join(this.methods);
        }
    }
}
