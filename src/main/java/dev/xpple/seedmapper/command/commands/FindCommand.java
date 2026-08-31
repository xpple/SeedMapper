package dev.xpple.seedmapper.command.commands;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.Generator;
import com.google.common.math.BigIntegerMath;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.feature.BuriedTreasureClusterData;
import dev.xpple.seedmapper.util.ComponentUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.xpple.seedmapper.thread.LocatorThreadHelper.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.*;

public class FindCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:find")
            .then(literal("cluster")
                .then(literal("buried_treasure")
                    .executes(ctx -> submit(() -> findBuriedTreasureCluster(CustomClientCommandSource.of(ctx.getSource())))))));
    }

    /*
     * we have a formationStructureSeed that works at coordinates (x, z)
     * however, we want it to work in our seed, meaning the coordinates change:
     * state = x*c_x + z*c_z + formationStructureSeed + salt (mod 2^48)
     * state = (x*c_x + z*c_z + formationStructureSeed - structureSeed) + structureSeed + salt (mod 2^48)
     * let offset = formationStructureSeed - structureSeed
     * state = (x*c_x + z*c_z + offset) + structureSeed + salt (mod 2^48)
     * write offset = x_0*c_x + z_0*c_z through EEA
     * state = (x*c_x + z*c_z + x_0*c_x + z_0*c_z) + structureSeed + salt (mod 2^48)
     * state = ((x + x_0)*c_x + (z + z_0)*c_z) + structureSeed + salt (mod 2^48)
     * new coordinates -> (x + x_0, z + z_0)
     * however, these coordinates won't be within the world border
     * to find small coordinates, use lattice reduction
     * we can use WolframAlpha for this:
     * LLL[{{-c_z, c_x}, {0, 2^48}, {2^48, 0}}]
     * ={{-12354965, -2831608}, {-989088, -23009024}}
     * the above is a change of basis matrix with relatively small, almost orthogonal vectors
     * if we let u = (-12354965, -989088) and v = (-2831608, -23009024)
     * then for every integer s, t we obtain a valid solution of the form
     * x = x_0 + s u_1 + t v_1, z = z_0 + s u_2 + t v_2
     * in matrix form, this is X = X_0 + B S
     * to find solutions within the world border, note that it is defined by its four corners
     * thus we need to find the coordinates of these corners in the new basis
     * these can be computed using S = B^-1 (X - X_0)
     * from here we can find the range of s (or t) values using the min and max of the corners
     * we iterate over each s, and using the inequalities |x| <= M and |y| <= M (for M = 30_000_000 / 16),
     * we can find the range of t values for which we have a valid point
     * at last we need to perform a biome check to make sure the buried treasures can actually generate
     */
    private static int findBuriedTreasureCluster(CustomClientCommandSource source) throws CommandSyntaxException {
        int version = source.getVersion();
        long seed = source.getSeed().getSecond().seed();

        long mask48 = (1L << 48) - 1;
        long structureSeed = seed & mask48;

        BigInteger twoPow48 = BigInteger.valueOf(1L << 48);

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment generator = Generator.allocate(arena);
            Cubiomes.setupGenerator(generator, version, 0);
            Cubiomes.applySeed(generator, Cubiomes.DIM_OVERWORLD(), seed);

            // much of this code can be optimised, but since these clusters are so rare it doesn't matter
            for (BuriedTreasureClusterData.FormationEntry formationEntry : BuriedTreasureClusterData.FORMATION_ENTRIES) {
                for (long formationStructureSeed : formationEntry.structureSeeds()) {
                    long offset = formationStructureSeed - structureSeed;
                    BigInteger factor = BigInteger.valueOf(offset);
                    // precomputed initial x and z using EEA
                    BigInteger x0 = BigInteger.valueOf(49284120807L).multiply(factor);
                    BigInteger z0 = BigInteger.valueOf(-126780825563L).multiply(factor);

                    BigInteger u1 = BigInteger.valueOf(-12354965L);
                    BigInteger u2 = BigInteger.valueOf(-989088L);
                    BigInteger v1 = BigInteger.valueOf(-2831608L);
                    BigInteger v2 = BigInteger.valueOf(-23009024L);

                    BigInteger M = BigInteger.valueOf(Level.MAX_LEVEL_SIZE).shiftRight(4);

                    List<Pair<BigInteger, BigInteger>> corners = List.of(Pair.of(M, M), Pair.of(M.negate(), M), Pair.of(M.negate(), M.negate()), Pair.of(M, M.negate()));
                    List<Pair<BigInteger, BigInteger>> transformedCorners = new ArrayList<>(corners.size());
                    for (Pair<BigInteger, BigInteger> corner : corners) {
                        BigInteger dx = corner.getFirst().subtract(x0);
                        BigInteger dz = corner.getSecond().subtract(z0);

                        // division by 2^48 omitted until later
                        BigInteger s = v2.multiply(dx).subtract(u2.multiply(dz));
                        BigInteger t = u1.multiply(dz).subtract(v1.multiply(dx));

                        transformedCorners.add(Pair.of(s, t));
                    }

                    BigInteger sMin = transformedCorners.stream().min(Comparator.comparing(Pair::getFirst, BigInteger::compareTo)).orElseThrow().getFirst();
                    BigInteger sMax = transformedCorners.stream().max(Comparator.comparing(Pair::getFirst, BigInteger::compareTo)).orElseThrow().getFirst();

                    // divide by 2^48
                    sMin = BigIntegerMath.divide(sMin, twoPow48, RoundingMode.CEILING);
                    sMax = BigIntegerMath.divide(sMax, twoPow48, RoundingMode.FLOOR);

                    for (BigInteger s = sMin; s.compareTo(sMax) <= 0; s = s.add(BigInteger.ONE)) {
                        BigInteger Ax = x0.add(s.multiply(u1));
                        BigInteger Az = z0.add(s.multiply(v1));

                        BigInteger txMin = BigIntegerMath.divide(M.subtract(Ax), u2, RoundingMode.CEILING);
                        BigInteger txMax = BigIntegerMath.divide(M.negate().subtract(Ax), u2, RoundingMode.FLOOR);
                        BigInteger tzMin = BigIntegerMath.divide(M.subtract(Az), v2, RoundingMode.CEILING);
                        BigInteger tzMax = BigIntegerMath.divide(M.negate().subtract(Az), v2, RoundingMode.FLOOR);

                        BigInteger tMin = txMin.max(tzMin);
                        BigInteger tMax = txMax.min(tzMax);

                        tLoop: for (BigInteger t = tMin; t.compareTo(tMax) <= 0; t = t.add(BigInteger.ONE)) {
                            // at this point we know that x and z must fit in Java ints
                            int x = Ax.add(t.multiply(u2)).intValueExact();
                            int z = Az.add(t.multiply(v2)).intValueExact();

                            for (ChunkPos pos : formationEntry.formation()) {
                                int biome = Cubiomes.getBiomeAt(generator, 4, ((x + pos.x()) << 2) + 2, 319 >> 2, ((z + pos.z()) << 2) + 2);
                                if (Cubiomes.isViableFeatureBiome(Generator.mc(generator), Cubiomes.Treasure(), biome) == 0) {
                                    continue tLoop;
                                }
                            }

                            source.getClient().schedule(() -> source.sendFeedback(Component.translatable("command.find.cluster.buried_treasure.success", ComponentUtils.formatXZ(x << 4, z << 4))));
                        }
                    }
                }
            }

            source.getClient().schedule(() -> source.sendFeedback(Component.translatable("command.find.cluster.buried_treasure.searchEnded")));
        }

        return Command.SINGLE_SUCCESS;
    }
}
