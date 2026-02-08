package dev.xpple.seedmapper.command.arguments;

import com.github.cubiomes.Cubiomes;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.xpple.seedmapper.command.CommandExceptions;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class VersionArgument implements ArgumentType<Integer> {

    private static final Collection<String> EXAMPLES = Arrays.asList("1.21.1", "1.17.1", "1.15");

    //<editor-fold defaultstate="collapsed" desc="static final Map<String, Integer> VERSIONS;">
    static final Map<String, Integer> VERSIONS = ImmutableMap.<String, Integer>builder()
        .put("b1.7", Cubiomes.MC_B1_7())
        .put("b1.7.2", Cubiomes.MC_B1_7())
        .put("b1.7.3", Cubiomes.MC_B1_7())
        .put("b1.8", Cubiomes.MC_B1_8())
        .put("b1.8.1", Cubiomes.MC_B1_8())
        .put("1.0", Cubiomes.MC_1_0())
        .put("1.1", Cubiomes.MC_1_1())
        .put("1.2", Cubiomes.MC_1_2_5())
        .put("1.2.1", Cubiomes.MC_1_2_5())
        .put("1.2.2", Cubiomes.MC_1_2_5())
        .put("1.2.3", Cubiomes.MC_1_2_5())
        .put("1.2.4", Cubiomes.MC_1_2_5())
        .put("1.2.5", Cubiomes.MC_1_2_5())
        .put("1.3", Cubiomes.MC_1_3_2())
        .put("1.3.1", Cubiomes.MC_1_3_2())
        .put("1.3.2", Cubiomes.MC_1_3_2())
        .put("1.4", Cubiomes.MC_1_4_7())
        .put("1.4.2", Cubiomes.MC_1_4_7())
        .put("1.4.4", Cubiomes.MC_1_4_7())
        .put("1.4.5", Cubiomes.MC_1_4_7())
        .put("1.4.6", Cubiomes.MC_1_4_7())
        .put("1.4.7", Cubiomes.MC_1_4_7())
        .put("1.5", Cubiomes.MC_1_5_2())
        .put("1.5.1", Cubiomes.MC_1_5_2())
        .put("1.5.2", Cubiomes.MC_1_5_2())
        .put("1.6", Cubiomes.MC_1_6_4())
        .put("1.6.1", Cubiomes.MC_1_6_4())
        .put("1.6.2", Cubiomes.MC_1_6_4())
        .put("1.6.4", Cubiomes.MC_1_6_4())
        .put("1.7", Cubiomes.MC_1_7_10())
        .put("1.7.2", Cubiomes.MC_1_7_10())
        .put("1.7.3", Cubiomes.MC_1_7_10())
        .put("1.7.4", Cubiomes.MC_1_7_10())
        .put("1.7.5", Cubiomes.MC_1_7_10())
        .put("1.7.6", Cubiomes.MC_1_7_10())
        .put("1.7.7", Cubiomes.MC_1_7_10())
        .put("1.7.8", Cubiomes.MC_1_7_10())
        .put("1.7.9", Cubiomes.MC_1_7_10())
        .put("1.7.10", Cubiomes.MC_1_7_10())
        .put("1.8", Cubiomes.MC_1_8_9())
        .put("1.8.1", Cubiomes.MC_1_8_9())
        .put("1.8.2", Cubiomes.MC_1_8_9())
        .put("1.8.3", Cubiomes.MC_1_8_9())
        .put("1.8.4", Cubiomes.MC_1_8_9())
        .put("1.8.5", Cubiomes.MC_1_8_9())
        .put("1.8.6", Cubiomes.MC_1_8_9())
        .put("1.8.7", Cubiomes.MC_1_8_9())
        .put("1.8.8", Cubiomes.MC_1_8_9())
        .put("1.8.9", Cubiomes.MC_1_8_9())
        .put("1.9", Cubiomes.MC_1_9_4())
        .put("1.9.1", Cubiomes.MC_1_9_4())
        .put("1.9.2", Cubiomes.MC_1_9_4())
        .put("1.9.3", Cubiomes.MC_1_9_4())
        .put("1.9.4", Cubiomes.MC_1_9_4())
        .put("1.10", Cubiomes.MC_1_10_2())
        .put("1.10.1", Cubiomes.MC_1_10_2())
        .put("1.10.2", Cubiomes.MC_1_10_2())
        .put("1.11", Cubiomes.MC_1_11_2())
        .put("1.11.1", Cubiomes.MC_1_11_2())
        .put("1.11.2", Cubiomes.MC_1_11_2())
        .put("1.12", Cubiomes.MC_1_12_2())
        .put("1.12.1", Cubiomes.MC_1_12_2())
        .put("1.12.2", Cubiomes.MC_1_12_2())
        .put("1.13", Cubiomes.MC_1_13_2())
        .put("1.13.1", Cubiomes.MC_1_13_2())
        .put("1.13.2", Cubiomes.MC_1_13_2())
        .put("1.14", Cubiomes.MC_1_14_4())
        .put("1.14.1", Cubiomes.MC_1_14_4())
        .put("1.14.2", Cubiomes.MC_1_14_4())
        .put("1.14.3", Cubiomes.MC_1_14_4())
        .put("1.14.4", Cubiomes.MC_1_14_4())
        .put("1.15", Cubiomes.MC_1_15_2())
        .put("1.15.1", Cubiomes.MC_1_15_2())
        .put("1.15.2", Cubiomes.MC_1_15_2())
        .put("1.16", Cubiomes.MC_1_16_1())
        .put("1.16.1", Cubiomes.MC_1_16_1())
        .put("1.16.2", Cubiomes.MC_1_16_1())
        .put("1.16.3", Cubiomes.MC_1_16_1())
        .put("1.16.4", Cubiomes.MC_1_16_1())
        .put("1.16.5", Cubiomes.MC_1_16_5())
        .put("1.17", Cubiomes.MC_1_17_1())
        .put("1.17.1", Cubiomes.MC_1_17_1())
        .put("1.18", Cubiomes.MC_1_18_2())
        .put("1.18.1", Cubiomes.MC_1_18_2())
        .put("1.18.2", Cubiomes.MC_1_18_2())
        .put("1.19", Cubiomes.MC_1_19_2())
        .put("1.19.1", Cubiomes.MC_1_19_2())
        .put("1.19.2", Cubiomes.MC_1_19_2())
        .put("1.19.3", Cubiomes.MC_1_19_2())
        .put("1.19.4", Cubiomes.MC_1_19_4())
        .put("1.20", Cubiomes.MC_1_20_6())
        .put("1.20.1", Cubiomes.MC_1_20_6())
        .put("1.20.2", Cubiomes.MC_1_20_6())
        .put("1.20.3", Cubiomes.MC_1_20_6())
        .put("1.20.4", Cubiomes.MC_1_20_6())
        .put("1.20.5", Cubiomes.MC_1_20_6())
        .put("1.20.6", Cubiomes.MC_1_20_6())
        .put("1.21", Cubiomes.MC_1_21_1())
        .put("1.21.1", Cubiomes.MC_1_21_1())
        .put("1.21.2", Cubiomes.MC_1_21_1())
        .put("1.21.3", Cubiomes.MC_1_21_3())
        .put("1.21.4", Cubiomes.MC_1_21_4())
        .put("1.21.5", Cubiomes.MC_1_21_5())
        .put("1.21.6", Cubiomes.MC_1_21_5())
        .put("1.21.7", Cubiomes.MC_1_21_5())
        .put("1.21.8", Cubiomes.MC_1_21_5())
        .put("1.21.9", Cubiomes.MC_1_21_9())
        .put("1.21.10", Cubiomes.MC_1_21_9())
        .put("1.21.11", Cubiomes.MC_1_21_11())
        .build();
    //</editor-fold>

    public static VersionArgument version() {
        return new VersionArgument();
    }

    public static int getVersion(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String versionString = reader.readUnquotedString();
        Integer version = VERSIONS.get(versionString);
        if (version == null) {
            reader.setCursor(cursor);
            throw CommandExceptions.UNKNOWN_VERSION_EXCEPTION.create(versionString);

        }
        return version;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(VERSIONS.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
