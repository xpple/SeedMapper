package dev.xpple.seedmapper.command.commands;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.levelgen.WorldOptions;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.*;

public class SeedInfoCommand {

    private static final long MASK32 = (1L << 32) - 1;
    private static final long MASK48 = (1L << 48) - 1;

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:seedinfo")
            .executes(ctx -> seedInfo(CustomClientCommandSource.of(ctx.getSource()))));
    }

    private static int seedInfo(CustomClientCommandSource source) throws CommandSyntaxException {
        long seed = source.getSeed().getSecond().seed();

        MutableComponent yesComponent = Component.translatable("command.seedinfo.yes").withStyle(ChatFormatting.GREEN);
        MutableComponent noComponent = Component.translatable("command.seedinfo.no").withStyle(ChatFormatting.RED);

        MutableComponent isTextSeedComponent;
        // TODO replace with `seed instanceof int`
        if ((int) seed == seed) {
            isTextSeedComponent = Component.translatable("command.seedinfo.isTextSeed", yesComponent);
        } else {
            isTextSeedComponent = Component.translatable("command.seedinfo.isTextSeed", noComponent);
        }

        MutableComponent isRandomSeedComponent;
        if (isRandom(seed)) {
            isRandomSeedComponent = Component.translatable("command.seedinfo.isRandomSeed", yesComponent);
        } else {
            isRandomSeedComponent = Component.translatable("command.seedinfo.isRandomSeed", noComponent);
        }

        source.sendFeedback(Component.translatable("command.seedinfo.info"));
        source.sendFeedback(isTextSeedComponent);
        source.sendFeedback(isRandomSeedComponent);
        return (int) seed;
    }

    /// Determines whether a seed could possibly have been randomly created through
    /// [WorldOptions#randomSeed()]. This is the case for `2^48` out of all `2^64` seeds.
    ///
    /// If the seed is randomly created, it is the return value of a `nextLong` call. `nextLong`
    /// returns `((long)(next(32)) << 32) + next(32)`. We check whether the lower and upper bits
    /// of `seed` agree. That is, the lower bits must be produced by a state that is one
    /// statement advancement from the state that produced the upper bits. Specifically,
    /// `next(32)` returns `((state * 0x5deece66d + 0xb) & MASK48) >>> 16`. Thus, for 16 uncertain
    /// bits, we have `(upper32 <<< 16) | upper16 == state * 0x5deece66d + 0xb (mod 2^48)`. We can
    /// rewrite this to `upperState = (((upper32 << 16) | upper16) - 0xb) * 0xdfe05bcb1365
    /// & MASK48`. From there we can derive `lowerState` by advancing once: `lowerState =
    /// (upperState * 0x5deece66dL + 0xb) & MASK48`. Using this state, we can compute half of
    /// `nextLong`. At last we check whether this value agrees with the observed lower 32 bits.
    ///
    /// Note that this can be done more efficiently using lattices. For that, see e.g.
    /// [`NextLongReverser.java`](https://github.com/SeedFinding/mc_core_java/blob/refs/heads/main/src/main/java/com/seedfinding/mccore/util/math/NextLongReverser.java).
    /// For my usecase however, this is fine.
    @VisibleForTesting
    public static boolean isRandom(long seed) {
        long lower32 = seed & MASK32;
        long upper32 = ((seed >>> 32) + (lower32 >>> 31)) & MASK32;

        for (long upper16 = 0; upper16 < 1L << 16; upper16++) {
            long upper48 = (upper32 << 16) | upper16;
            long upperState = ((upper48 - 0xb) * 0xdfe05bcb1365L) & MASK48;
            // go forward one state
            long lowerState = (upperState * 0x5deece66dL + 0xb) & MASK48;

            if (((lowerState * 0x5deece66dL + 0xb) & MASK48) >>> 16 == lower32) {
                return true;
            }
        }
        return false;
    }
}
