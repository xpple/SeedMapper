package dev.xpple.seedmapper;

import dev.xpple.seedmapper.command.commands.SeedInfoCommand;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class IsRandomSeedTest {
    @Test
    public void testRandomSeeds() {
        // differential tests
        Random seedRandom = new Random(0);

        for (int i = 0; i < 100; i++) {
            Random random = new Random(seedRandom.nextLong());

            for (int j = 0; j < 100; j++) {
                assertTrue(SeedInfoCommand.isRandom(random.nextLong()));
            }
        }

        // example-based tests
        assertTrue(SeedInfoCommand.isRandom(-9174856207352116915L));
        assertTrue(SeedInfoCommand.isRandom(0L));
    }

    @Test
    public void testNonrandomSeeds() {
        long[] notFromNextLong = {
            0x0000_0000_0000_0001L, 0x0000_0000_0000_0002L, 0x0000_0000_0000_0003L, 0x0000_0000_0000_0004L, 0xFFFF_FFFF_FFFF_FFFFL,
            0x8000_0000_0000_0000L, 0x7FFF_FFFF_FFFF_FFFFL, 0x0000_0000_119C_4EA3L, 0xBB20_B460_D4D9_5138L, 0x42DF_AC23_D094_371EL,
            0xC65A_9F3F_C3E4_7086L, 0x7FCB_F859_FFAA_AC94L, 0xFFD9_C832_6B72_1E35L, 0x93B1_94B1_AECE_54CEL, 0x0C9A_024E_9342_CFB0L,
            0xB5E2_19A5_0CF9_1A93L, 0x953C_39EF_CABE_06F0L, 0xED3B_2742_EF74_B6A3L, 0x4668_DC66_26EF_FD8AL, 0x87BB_6806_FF70_383AL,
            0x3F19_279C_B15E_D302L, 0x2878_0310_9FD5_F082L, 0x4185_D659_D679_B6E4L, 0xE1BE_B49E_D5BC_248CL, 0x1E48_0C7B_5DE3_0B3BL,
            0xCDBF_BC82_C514_121BL, 0xD339_E99C_E98C_4F78L, 0x1A90_1512_5AF1_2891L, 0x0D09_F3A0_B455_6D04L, 0x452B_B60E_6FA0_8C9DL,
            0x206F_5712_6824_5C72L, 0xF540_028B_ED6A_335BL, 0xC3AE_DB31_17FB_1219L, 0xFCB1_C0CE_0F72_516FL, 0xA14F_DA4F_4D75_A488L,
            0x7B24_AFA9_58F1_B0D1L, 0x2556_0745_C7D8_047BL, 0x664A_A335_8723_CEEDL, 0xD0A2_3E2C_33E5_F1F0L, 0x7613_DDC7_2C4D_FC97L,
            0x5B1F_5269_6172_D845L, 0x6537_FD7A_0409_5C7EL, 0x8BDF_707A_AF1D_182DL, 0x64D0_3FF9_6E9D_3EB1L, 0x7282_54CA_3C38_D539L,
            0x4736_B34F_325C_F188L, 0x441B_B348_1130_F543L, 0x5B64_6697_6175_5F03L, 0xA7D2_CB75_3793_5205L, 0x1601_5CD9_57BA_6AF0L
        };

        for (long seed : notFromNextLong) {
            assertFalse(SeedInfoCommand.isRandom(seed));
        }

        assertFalse(SeedInfoCommand.isRandom(1551515151585454L));
    }
}
