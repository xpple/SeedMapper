package dev.xpple.seedmapper;

import dev.xpple.seedmapper.command.commands.SeedInfoCommand;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TextSeedReversalTest {
    @Test
    public void testRandom() {
        Random seedRandom = new Random(0);

        for (int i = 0; i < 100; i++) {
            Random random = new Random(seedRandom.nextLong());

            for (int j = 0; j < 100; j++) {
                byte[] bytes = new byte[10];
                random.nextBytes(bytes);
                String string = new String(bytes, StandardCharsets.UTF_8);
                int hashCode = string.hashCode();
                for (String s : SeedInfoCommand.reverseTextSeed(hashCode)) {
                    assertEquals(hashCode, s.hashCode());
                }
            }
        }
    }
}
