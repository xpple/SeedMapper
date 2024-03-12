package dev.xpple.seedmapper.simulation;

import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

public class FakeWorldGenerationProgressListener implements WorldGenerationProgressListener {

    public static final FakeWorldGenerationProgressListener INSTANCE = new FakeWorldGenerationProgressListener();

    private FakeWorldGenerationProgressListener() {
    }

    @Override
    public void start(ChunkPos spawnPos) {
    }

    @Override
    public void setChunkStatus(ChunkPos pos, @Nullable ChunkStatus status) {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }
}
