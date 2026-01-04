package dev.xpple.seedmapper.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.Util;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DurationArgument implements ArgumentType<Duration> {

    private static final Collection<String> EXAMPLES = List.of("5s", "10m", "3h");

    private static final SimpleCommandExceptionType INVALID_UNIT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.time.invalid_unit"));

    public static DurationArgument duration() {
        return new DurationArgument();
    }

    public static DurationArgument getDuration(CommandContext<CustomClientCommandSource> context, String name) {
        return context.getArgument(name, DurationArgument.class);
    }

    @Override
    public Duration parse(StringReader reader) throws CommandSyntaxException {
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

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static final class Parser {

        private static final Object2IntMap<String> UNITS = Util.make(() -> {
            Object2IntMap<String> temp = new Object2IntOpenHashMap<>();

            temp.put("s", 1);
            temp.put("m", TimeUtil.SECONDS_PER_MINUTE);
            temp.put("h", (int) TimeUtil.SECONDS_PER_HOUR);

            return Object2IntMaps.unmodifiable(temp);
        });

        private final StringReader reader;
        private Consumer<SuggestionsBuilder> suggester;

        private Parser(StringReader reader) {
            this.reader = reader;
        }

        private Duration parse() throws CommandSyntaxException {
            float durationRaw = reader.readFloat();
            int cursor = reader.getCursor();
            suggester = builder -> {
                SuggestionsBuilder newBuilder = builder.createOffset(cursor);
                SharedSuggestionProvider.suggest(UNITS.keySet(), newBuilder);
                builder.add(newBuilder);
            };
            String string = reader.readUnquotedString();
            int unit = UNITS.getOrDefault(string, -1);
            if (unit == -1) {
                throw INVALID_UNIT_EXCEPTION.create();
            }
            int durationSeconds = Math.round(durationRaw * unit);
            return Duration.ofSeconds(durationSeconds);
        }
    }
}
