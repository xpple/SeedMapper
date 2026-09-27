package dev.xpple.seedmapper.command.commands;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.util.ExtraComponentUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.levelgen.WorldOptions;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

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
        boolean isTextSeed = (int) seed == seed;
        if (isTextSeed) {
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

        source.sendFeedback(Component.translatable("command.seedinfo.info", ExtraComponentUtils.formatNumber(seed)));
        source.sendFeedback(isTextSeedComponent);
        if (isTextSeed) {
            Component texts = ComponentUtils.formatList(SeedInfoCommand.reverseTextSeed((int) seed), ExtraComponentUtils::formatString);
            source.sendFeedback(Component.translatable("command.seedinfo.possibleTexts", texts));
        }
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
    ///
    /// @param seed the world seed to check
    /// @return `true` iff the seed could have been randomly created
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

    /// Determines possible strings that could have produced the input text seed. When text is
    /// entered into the seed field, the game produces a seed based on the string by computing
    /// [String#hashCode()] (see [WorldOptions#parseSeed]). This conversion is lossy; there are
    /// many more possible strings than possible `int` values. It is therefore impossible to
    /// know which exact string produced the observed text seed. However, we can still generate
    /// plausible strings that produce the same hash code.
    ///
    /// In Java, [String#hashCode()] is computed as follows, where `n` is the length of the
    /// string.
    ///
    /// ```
    /// s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]*31^0
    /// ```
    ///
    /// As `textSeed` must be of this form, this expression essentially expresses `textSeed` in
    /// base `31` (as long as `0 <= s[i] < 31` for each `i`). A very basic reversal is therefore:
    ///
    /// ```java
    /// Long.toString(Integer.toUnsignedLong(textSeed), 31).chars()
    ///     .mapToObj(c -> (char) c)
    ///     .map(c -> (char) Integer.parseInt(String.valueOf(c), 31))
    ///     .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
    ///     .toString()
    /// ```
    ///
    /// However, this usually returns garbage text, which was likely not used to create the seed.
    /// This is because the characters returned are U+0000 (null character) through U+001E
    /// (information separator two). If we instead fix `0` to represent `'A'`, then the result
    /// will be much closer to readable text. The characters returned will be `'A'` to `'_'`. Let
    /// `a` be the char code of `'A'`. Then
    ///
    /// ```java
    /// s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]*31^0 = textSeed
    /// (s[0]+a)*31^(n-1) + (s[1]+a)*31^(n-2) + ... + (s[n-1]+a)*31^0 = textSeed + a(31^(n-1) + 31^(n-2) + ... + 31^0)
    /// (s[0]+a)*31^(n-1) + (s[1]+a)*31^(n-2) + ... + (s[n-1]+a)*31^0 = textSeed + "A".repeat(n).hashCode()
    /// ```
    ///
    /// To find texts in this character range, we must therefore subtract `"A".repeat(n).hashCode()`,
    /// and left-pad the resulting string with `"A"`. We still have a choice to make, and that is
    /// the length of the string. To ensure we can find a string for every input, it should hold
    /// that `31^n >= 2^32`, and so `n >= log_31(2^32)` which gives `n >= 7`.
    ///
    /// To get more results, we can do at least four things:
    ///
    /// - find preimages of `textSeed + k * 2^32`,
    /// - try lower values of `n`,
    /// - increase `n`,
    /// - use different starting characters.
    ///
    /// The below code implements the first two options.
    ///
    /// @param textSeed the text seed to check
    /// @return strings that could have produced the text seed
    @VisibleForTesting
    public static Set<String> reverseTextSeed(int textSeed) {
        final int base = 31;
        final char start = 'A';

        Set<String> results = new HashSet<>();
        BigInteger capacity = BigInteger.ONE;
        for (int n = 0; n <= 7; n++, capacity = capacity.multiply(BigInteger.valueOf(base))) {
            int offset = String.valueOf(start).repeat(n).hashCode();
            BigInteger value = BigInteger.valueOf(Integer.toUnsignedLong(textSeed - offset));
            if (value.compareTo(capacity) >= 0) {
                continue;
            }
            BigInteger modulus = BigInteger.ONE.shiftLeft(Integer.SIZE);
            BigInteger radix = BigInteger.valueOf(base);

            while (value.compareTo(capacity) < 0) {
                char[] chars = new char[n];
                BigInteger remaining = value;

                for (int i = n - 1; i >= 0; i--) {
                    BigInteger[] qr = remaining.divideAndRemainder(radix);
                    chars[i] = (char) (start + qr[1].intValue());
                    remaining = qr[0];
                }

                results.add(new String(chars));
                value = value.add(modulus);
            }
        }
        return results;
    }
}
