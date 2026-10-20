package dev.xpple.seedmapper.render;

import com.google.common.cache.CacheBuilder;
import dev.xpple.seedmapper.config.Configs;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
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

    public static void drawCancellingBoxes(Collection<BlockPos> posBatch, int color) {
        Set<Line> lines = new HashSet<>();

        posBatch.forEach(pos -> {
            Vec3 minPosition = new Vec3(pos);
            Vec3 size = new Vec3(1, 1, 1);
            addCancellingLine(new Line(minPosition, minPosition.add(size.x(), 0, 0), color), lines);
            addCancellingLine(new Line(minPosition, minPosition.add(0, size.y(), 0), color), lines);
            addCancellingLine(new Line(minPosition, minPosition.add(0, 0, size.z()), color), lines);
            addCancellingLine(new Line(minPosition.add(size.x(), 0, size.z()), minPosition.add(size.x(), 0, 0), color), lines);
            addCancellingLine(new Line(minPosition.add(size.x(), 0, size.z()), minPosition.add(size.x(), size.y(), size.z()), color), lines);
            addCancellingLine(new Line(minPosition.add(size.x(), 0, size.z()), minPosition.add(0, 0, size.z()), color), lines);
            addCancellingLine(new Line(minPosition.add(size.x(), size.y(), 0), minPosition.add(size.x(), 0, 0), color), lines);
            addCancellingLine(new Line(minPosition.add(size.x(), size.y(), 0), minPosition.add(0, size.y(), 0), color), lines);
            addCancellingLine(new Line(minPosition.add(size.x(), size.y(), 0), minPosition.add(size.x(), size.y(), size.z()), color), lines);
            addCancellingLine(new Line(minPosition.add(0, size.y(), size.z()), minPosition.add(0, 0, size.z()), color), lines);
            addCancellingLine(new Line(minPosition.add(0, size.y(), size.z()), minPosition.add(0, size.y(), 0), color), lines);
            addCancellingLine(new Line(minPosition.add(0, size.y(), size.z()), minPosition.add(size.x(), size.y(), size.z()), color), lines);
        });
        RenderManager.lines.addAll(lines);
    }

    public static void drawRectangles(Collection<AABB> rectangles, int color) {
        for (AABB rectangle : rectangles) {
            double y = (rectangle.minY + rectangle.maxY) / 2;
            Vec3 minPosition = new Vec3(rectangle.minX, y, rectangle.minZ);
            Vec3 maxPosition = new Vec3(rectangle.maxX, y, rectangle.maxZ);
            lines.add(new Line(minPosition, new Vec3(maxPosition.x, y, minPosition.z), color));
            lines.add(new Line(minPosition, new Vec3(minPosition.x, y, maxPosition.z), color));
            lines.add(new Line(maxPosition, new Vec3(maxPosition.x, y, minPosition.z), color));
            lines.add(new Line(maxPosition, new Vec3(minPosition.x, y, maxPosition.z), color));
        }
    }

    public static void drawCancellingFaces(Collection<BlockPos> posBatch, int color, Direction direction) {
        Set<Line> lines = new HashSet<>();

        posBatch.forEach(pos -> {
            Vec3 minPosition = new Vec3(pos);
            Vec3 maxPosition = minPosition.add(1, 1, 1);

            switch (direction) {
                case DOWN -> {
                    addCancellingLine(new Line(minPosition, minPosition.add(1, 0, 0), color), lines);
                    addCancellingLine(new Line(minPosition, minPosition.add(0, 0, 1), color), lines);
                    addCancellingLine(new Line(minPosition.add(1, 0, 1), minPosition.add(1, 0, 0), color), lines);
                    addCancellingLine(new Line(minPosition.add(1, 0, 1), minPosition.add(0, 0, 1), color), lines);
                }
                case UP -> {
                    addCancellingLine(new Line(minPosition.add(0, 1, 0), minPosition.add(1, 1, 0), color), lines);
                    addCancellingLine(new Line(minPosition.add(0, 1, 0), minPosition.add(0, 1, 1), color), lines);
                    addCancellingLine(new Line(maxPosition, minPosition.add(1, 1, 0), color), lines);
                    addCancellingLine(new Line(maxPosition, minPosition.add(0, 1, 1), color), lines);
                }
                case NORTH -> {
                    addCancellingLine(new Line(minPosition, minPosition.add(1, 0, 0), color), lines);
                    addCancellingLine(new Line(minPosition, minPosition.add(0, 1, 0), color), lines);
                    addCancellingLine(new Line(minPosition.add(1, 1, 0), minPosition.add(1, 0, 0), color), lines);
                    addCancellingLine(new Line(minPosition.add(1, 1, 0), minPosition.add(0, 1, 0), color), lines);
                }
                case SOUTH -> {
                    addCancellingLine(new Line(minPosition.add(0, 0, 1), minPosition.add(1, 0, 1), color), lines);
                    addCancellingLine(new Line(minPosition.add(0, 0, 1), minPosition.add(0, 1, 1), color), lines);
                    addCancellingLine(new Line(maxPosition, minPosition.add(1, 0, 1), color), lines);
                    addCancellingLine(new Line(maxPosition, minPosition.add(0, 1, 1), color), lines);
                }
                case WEST -> {
                    addCancellingLine(new Line(minPosition, minPosition.add(0, 1, 0), color), lines);
                    addCancellingLine(new Line(minPosition, minPosition.add(0, 0, 1), color), lines);
                    addCancellingLine(new Line(minPosition.add(0, 1, 1), minPosition.add(0, 0, 1), color), lines);
                    addCancellingLine(new Line(minPosition.add(0, 1, 1), minPosition.add(0, 1, 0), color), lines);
                }
                case EAST -> {
                    addCancellingLine(new Line(minPosition.add(1, 0, 0), minPosition.add(1, 1, 0), color), lines);
                    addCancellingLine(new Line(minPosition.add(1, 0, 0), minPosition.add(1, 0, 1), color), lines);
                    addCancellingLine(new Line(maxPosition, minPosition.add(1, 1, 0), color), lines);
                    addCancellingLine(new Line(maxPosition, minPosition.add(1, 0, 1), color), lines);
                }
            }
        });
        RenderManager.lines.addAll(lines);
    }

    public static void clear() {
        lines.clear();
    }

    private static void addCancellingLine(Line line, Set<Line> lines) {
        if (!lines.add(line)) {
            lines.remove(line);
        }
    }

    public static void registerEvents() {
        LevelExtractionEvents.END_EXTRACTION.register(RenderManager::extractLines);
        LevelRenderEvents.BEFORE_GIZMOS.register(RenderManager::renderLines);
    }

    private static void extractLines(LevelExtractionContext levelExtractionContext) {
        Set<Line> extractedLines = new HashSet<>();
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        RenderManager.lines.forEach(line -> {
            ChunkPos chunkPos = ChunkPos.containing(BlockPos.containing(line.start()));
            if (level.getChunk(chunkPos.x(), chunkPos.z(), ChunkStatus.FULL, false) == null) {
                return;
            }
            extractedLines.add(line.offset(levelExtractionContext.camera().position().scale(-1)));
        });
        levelExtractionContext.levelState().setData(LINES_SET_KEY, extractedLines);
    }

    private static void renderLines(LevelRenderContext context) {
        Set<Line> extractedLines = context.levelState().getData(LINES_SET_KEY);
        if (extractedLines == null) {
            return;
        }
        extractedLines.forEach(line -> {
            Vec3 start = line.start();
            Vec3 end = line.end();
            Vec3 normal = end.subtract(start).normalize();
            int color = line.color();
            float red = ARGB.redFloat(color);
            float green = ARGB.greenFloat(color);
            float blue = ARGB.blueFloat(color);

            if (context.submitNodeCollector().order(0) instanceof SubmitNodeCollection collection) {
                collection.afterTerrain.submit(new CustomFeatureRenderer.Submit(context.poseStack().last().copy(), NoDepthLayer.LINES_NO_DEPTH_LAYER, (pose, buffer) -> {
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
                }));
            }
        });
    }
}
