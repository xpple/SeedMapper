package dev.xpple.seedmapper.render;

import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xpple.seedmapper.config.Configs;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class RenderManager {

    private RenderManager() {
    }

    private static final RenderStateDataKey<Set<Line>> LINES_SET_KEY = RenderStateDataKey.create(() -> "SeedMapper lines set");

    private static Set<Line> lines = Collections.emptySet();
    static {
        rebuildLineSet();
    }

    public static void rebuildLineSet() {
        Set<Line> temp = lines;
        lines = Collections.newSetFromMap(CacheBuilder.newBuilder().expireAfterWrite(Configs.HighlightDuration).<Line, Boolean>build().asMap());
        lines.addAll(temp);
        temp.clear();
    }

    public static void drawBoxes(Collection<BlockPos> posBatch, int colour) {
        Set<Line> lines = new HashSet<>();

        posBatch.forEach(pos -> {
            Vec3 minPosition = new Vec3(pos);
            Vec3 size = new Vec3(1, 1, 1);
            addLine(new Line(minPosition, minPosition.add(size.x(), 0, 0), colour), lines);
            addLine(new Line(minPosition, minPosition.add(0, size.y(), 0), colour), lines);
            addLine(new Line(minPosition, minPosition.add(0, 0, size.z()), colour), lines);
            addLine(new Line(minPosition.add(size.x(), 0, size.z()), minPosition.add(size.x(), 0, 0), colour), lines);
            addLine(new Line(minPosition.add(size.x(), 0, size.z()), minPosition.add(size.x(), size.y(), size.z()), colour), lines);
            addLine(new Line(minPosition.add(size.x(), 0, size.z()), minPosition.add(0, 0, size.z()), colour), lines);
            addLine(new Line(minPosition.add(size.x(), size.y(), 0), minPosition.add(size.x(), 0, 0), colour), lines);
            addLine(new Line(minPosition.add(size.x(), size.y(), 0), minPosition.add(0, size.y(), 0), colour), lines);
            addLine(new Line(minPosition.add(size.x(), size.y(), 0), minPosition.add(size.x(), size.y(), size.z()), colour), lines);
            addLine(new Line(minPosition.add(0, size.y(), size.z()), minPosition.add(0, 0, size.z()), colour), lines);
            addLine(new Line(minPosition.add(0, size.y(), size.z()), minPosition.add(0, size.y(), 0), colour), lines);
            addLine(new Line(minPosition.add(0, size.y(), size.z()), minPosition.add(size.x(), size.y(), size.z()), colour), lines);
        });
        RenderManager.lines.addAll(lines);
    }

    public static void clear() {
        lines.clear();
    }

    private static void addLine(Line line, Set<Line> lines) {
        if (!lines.add(line)) {
            lines.remove(line);
        }
    }

    public static void registerEvents() {
        WorldRenderEvents.END_EXTRACTION.register(RenderManager::extractLines);
        WorldRenderEvents.END_MAIN.register(RenderManager::renderLines);
    }

    private static void extractLines(WorldExtractionContext worldExtractionContext) {
        Set<Line> extractedLines = new HashSet<>();
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        RenderManager.lines.forEach(line -> {
            ChunkPos chunkPos = new ChunkPos(BlockPos.containing(line.start()));
            if (level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false) == null) {
                return;
            }
            extractedLines.add(line.offset(worldExtractionContext.camera().position().scale(-1)));
        });
        worldExtractionContext.worldState().setData(LINES_SET_KEY, extractedLines);
    }

    private static void renderLines(WorldRenderContext worldRenderContext) {
        Set<Line> extractedLines = worldRenderContext.worldState().getData(LINES_SET_KEY);
        if (extractedLines == null) {
            return;
        }
        extractedLines.forEach(line -> {
            Vec3 start = line.start();
            Vec3 end = line.end();
            Vec3 normal = end.subtract(start).normalize();
            int colour = line.colour();
            float red = ARGB.redFloat(colour);
            float green = ARGB.greenFloat(colour);
            float blue = ARGB.blueFloat(colour);

            worldRenderContext.matrices().pushPose();
            PoseStack.Pose pose = worldRenderContext.matrices().last();
            VertexConsumer buffer = worldRenderContext.consumers().getBuffer(NoDepthLayer.LINES_NO_DEPTH_LAYER);
            buffer
                .addVertex(pose, (float) start.x, (float) start.y, (float) start.z)
                .setColor(red, green, blue, 1.0F)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z)
                .setLineWidth(2);
            buffer
                .addVertex(pose, (float) end.x, (float) end.y, (float) end.z)
                .setColor(red, green, blue, 1.0F)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z)
                .setLineWidth(2);
            worldRenderContext.matrices().popPose();
        });
    }
}
