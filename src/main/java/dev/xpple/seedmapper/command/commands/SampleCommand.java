package dev.xpple.seedmapper.command.commands;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.TerrainNoiseParameters;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.seedmapper.command.CommandExceptions;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.util.ComponentUtils;
import dev.xpple.seedmapper.util.SeedIdentifier;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static dev.xpple.seedmapper.command.arguments.DensityFunctionArgument.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class SampleCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:sample")
            .requires(_ -> Configs.DevMode)
            .then(literal("all")
                .executes(ctx -> sampleAll(CustomClientCommandSource.of(ctx.getSource()))))
            .then(literal("function")
                .then(argument("densityfunction", densityFunction())
                    .executes(ctx -> sampleDensity(CustomClientCommandSource.of(ctx.getSource()), getDensityFunction(ctx, "densityfunction"))))));
    }

    private static int sampleAll(CustomClientCommandSource source) throws CommandSyntaxException {
        SeedIdentifier seed = source.getSeed().getSecond();
        int dimension = source.getDimension();
        if (dimension != Cubiomes.DIM_OVERWORLD()) {
            throw CommandExceptions.INVALID_DIMENSION_EXCEPTION.create();
        }
        int version = source.getVersion();

        BlockPos pos = BlockPos.containing(source.getPosition());

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment params = TerrainNoiseParameters.allocate(arena);
            if (Cubiomes.initTerrainNoise(params, seed.seed(), version) == 0) {
                throw CommandExceptions.INCOMPATIBLE_PARAMETERS_EXCEPTION.create();
            }

            DENSITY_FUNCTIONS.forEach((key, densityFunction) -> {
                double density = densityFunction.compute(params, pos.getX(), pos.getY(), pos.getZ());
                source.sendFeedback(Component.translatable("command.sample.sampleAll.success", key, ComponentUtils.formatXYZ(pos.getX(), pos.getY(), pos.getZ()), ComponentUtils.formatNumber(density)));
            });

            return DENSITY_FUNCTIONS.size();
        }
    }

    private static int sampleDensity(CustomClientCommandSource source, DensityFunction densityFunction) throws CommandSyntaxException {
        SeedIdentifier seed = source.getSeed().getSecond();
        int dimension = source.getDimension();
        if (dimension != Cubiomes.DIM_OVERWORLD()) {
            throw CommandExceptions.INVALID_DIMENSION_EXCEPTION.create();
        }
        int version = source.getVersion();

        BlockPos pos = BlockPos.containing(source.getPosition());

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment params = TerrainNoiseParameters.allocate(arena);
            Cubiomes.initTerrainNoise(params, seed.seed(), version);
            double density = densityFunction.compute(params, pos.getX(), pos.getY(), pos.getZ());
            source.sendFeedback(Component.translatable("command.sample.sampleDensity.success", ComponentUtils.formatXYZ(pos.getX(), pos.getY(), pos.getZ()), ComponentUtils.formatNumber(density)));

            return (int) density;
        }
    }
}
