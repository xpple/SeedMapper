package dev.xpple.seedmapper.command.arguments;

import com.github.cubiomes.BiomeNoise;
import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.DoublePerlinNoise;
import com.github.cubiomes.Generator;
import com.github.cubiomes.TerrainNoise;
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

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DensityFunctionArgument implements ArgumentType<DensityFunctionArgument.DensityFunction> {

    private static final Collection<String> EXAMPLES = Arrays.asList("overworld", "the_nether", "the_end");

    public static final Map<String, DensityFunction> DENSITY_FUNCTIONS = ImmutableMap.<String, DensityFunction>builder()
        .put("overworld.base_3d_noise", (params, x, y, z) -> Cubiomes.sampleBase3dNoise(TerrainNoise.base3dNoise(params), x, y, z))
        .put("overworld.caves.spaghetti_roughness_function", Cubiomes::sampleSpaghettiRoughness)
        .put("overworld.caves.spaghetti_2d_thickness_modulator", Cubiomes::sampleSpaghetti2dThicknessModulator)
        .put("overworld.caves.spaghetti_2d", Cubiomes::sampleSpaghetti2d)
        .put("spaghetti_3d", Cubiomes::sampleSpaghetti3d)
        .put("cave_entrance", Cubiomes::sampleCaveEntrance)
        .put("overworld.caves.entrances", (params, x, y, z) -> Cubiomes.sampleEntrances(params, x, y, z, Cubiomes.sampleSpaghettiRoughness(params, x, y, z)))
        .put("cave_layer", Cubiomes::sampleCaveLayer)
        .put("overworld.sloped_cheese", DensityFunctionArgument::computeSlopedCheese)
        .put("cave_cheese", (params, x, y, z) -> Cubiomes.sampleCaveCheese(params, x, y, z, computeSlopedCheese(params, x, y, z)))
        .put("overworld.caves.pillars", Cubiomes::samplePillars)
        .put("overworld.caves.noodle", Cubiomes::sampleNoodle)
        .put("underground", (params, x, y, z) -> {
            double spaghettiRoughness = Cubiomes.sampleSpaghettiRoughness(params, x, y, z);
            return Cubiomes.sampleUnderground(params, x, y, z, spaghettiRoughness, Cubiomes.sampleEntrances(params, x, y, z, spaghettiRoughness), computeSlopedCheese(params, x, y, z));
        })
        .put("final_density", (params, x, y, z) -> {
            double spaghettiRoughness = Cubiomes.sampleSpaghettiRoughness(params, x, y, z);
            return Cubiomes.sampleFinalDensity(params, x, y, z, spaghettiRoughness, Cubiomes.sampleEntrances(params, x, y, z, spaghettiRoughness), computeSlopedCheese(params, x, y, z));
        })
        .put("preliminary_surface_level", (params, x, _, z) -> Cubiomes.samplePreliminarySurfaceLevel(params, x, z))
        .build();

    public static DensityFunctionArgument densityFunction() {
        return new DensityFunctionArgument();
    }

    public static DensityFunction getDensityFunction(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, DensityFunction.class);
    }

    @Override
    public DensityFunction parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String densityFunctionString = reader.readUnquotedString();
        DensityFunction densityFunction = DENSITY_FUNCTIONS.get(densityFunctionString);
        if (densityFunction == null) {
            reader.setCursor(cursor);
            throw CommandExceptions.UNKNOWN_DENSITY_FUNCTION_EXCEPTION.create(densityFunctionString);
        }
        return densityFunction;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(DENSITY_FUNCTIONS.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @FunctionalInterface
    public interface DensityFunction {
        double compute(MemorySegment terrainNoise, int x, int y, int z);
    }

    private static double computeSlopedCheese(MemorySegment params, int x, int y, int z) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment np_param = arena.allocate(Cubiomes.C_FLOAT, 4);
            Cubiomes.sampleNoiseParameters(Generator.bn(TerrainNoise.g(params)), x >> 2, z >> 2, np_param);
            double depth = Cubiomes.getSpline(BiomeNoise.sp(Generator.bn(TerrainNoise.g(params))), np_param) - 0.50375f;
            double factor = Cubiomes.getSpline(TerrainNoise.factorSpline(params), np_param);
            double jagged = Cubiomes.sampleDoublePerlin(TerrainNoise.noises(params).asSlice(Cubiomes.OTP_JAGGED() * DoublePerlinNoise.sizeof()), x * 1500.0, 0, z * 1500.0);
            jagged = jagged >= 0.0 ? jagged : jagged / 2.0;
            jagged *= Cubiomes.getSpline(TerrainNoise.jaggednessSpline(params), np_param);
            return Cubiomes.sampleSlopedCheese(params, x, y, z, depth, factor, jagged);
        }
    }
}
