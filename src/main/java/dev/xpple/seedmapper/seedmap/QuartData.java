package dev.xpple.seedmapper.seedmap;

import com.github.cubiomes.Cubiomes;
import dev.xpple.seedmapper.util.QuartPos2;
import net.minecraft.util.ARGB;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public record QuartData(QuartPos2 quartPos, int biome, int biomeColour) {
    // unsigned char color[3]
    private static final MemoryLayout RGB_LAYOUT = MemoryLayout.sequenceLayout(3, Cubiomes.C_CHAR);
    private static final MemorySegment biomeColours;

    static {
        biomeColours = Arena.global().allocate(RGB_LAYOUT, 256);
        Cubiomes.initBiomeColors(biomeColours);
    }

    public QuartData(QuartPos2 quartPos, int biome) {
        this(quartPos, biome, getBiomeColour(biome));
    }

    private static int getBiomeColour(int biome) {
        MemorySegment colourArray = biomeColours.asSlice(biome * RGB_LAYOUT.byteSize());
        int red = colourArray.getAtIndex(Cubiomes.C_CHAR, 0) & 0xFF;
        int green = colourArray.getAtIndex(Cubiomes.C_CHAR, 1) & 0xFF;
        int blue = colourArray.getAtIndex(Cubiomes.C_CHAR, 2) & 0xFF;
        return ARGB.color(red, green, blue);
    }
}
