package dev.xpple.seedmapper.feature;

import dev.xpple.seedmapper.SeedMapper;
import net.minecraft.world.level.ChunkPos;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

// all structure seeds that have a maximum cluster of 7 buried treasures at spawn
public final class BuriedTreasureClusterData {

    private static final List<String> FORMATIONS = List.of(
        "buried_treasure_formation_1.bin",
        "buried_treasure_formation_2.bin",
        "buried_treasure_formation_3.bin",
        "buried_treasure_formation_4.bin",
        "buried_treasure_formation_5.bin",
        "buried_treasure_formation_6.bin",
        "buried_treasure_formation_7.bin",
        "buried_treasure_formation_8.bin",
        "buried_treasure_formation_9.bin",
        "buried_treasure_formation_10.bin"
    );

    public static Stream<FormationEntry> access(Arena arena) {
        return FORMATIONS.stream()
            .map(formation -> SeedMapper.MOD_CONTAINER.findPath("store/" + formation).orElseThrow())
            .map(path -> {
                try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
                    MemorySegment segment = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size(), arena);
                    int formationSize = segment.get(ValueLayout.JAVA_INT.withOrder(ByteOrder.BIG_ENDIAN), 0);
                    segment = segment.asSlice(ValueLayout.JAVA_INT.byteSize());
                    List<ChunkPos> formation = new ArrayList<>(formationSize);
                    for (int i = 0; i < formationSize; i++) {
                        int chunkX = segment.get(ValueLayout.JAVA_INT.withOrder(ByteOrder.BIG_ENDIAN), 0);
                        segment = segment.asSlice(ValueLayout.JAVA_INT.byteSize());
                        int chunkZ = segment.get(ValueLayout.JAVA_INT.withOrder(ByteOrder.BIG_ENDIAN), 0);
                        segment = segment.asSlice(ValueLayout.JAVA_INT.byteSize());
                        formation.add(new ChunkPos(chunkX, chunkZ));
                    }
                    int structureSeedsLen = segment.get(ValueLayout.JAVA_INT.withOrder(ByteOrder.BIG_ENDIAN), 0);
                    segment = segment.asSlice(ValueLayout.JAVA_INT.byteSize());
                    return new FormationEntry(formation, structureSeedsLen, segment);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    public record FormationEntry(List<ChunkPos> formation, int structureSeedsLen, MemorySegment structureSeeds) {
    }
}
