package dev.xpple.seedmapper.render;

import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public final class RenderManager {

    private RenderManager() {
    }

    private static final Set<Line> lines = Collections.newSetFromMap(CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(5)).<Line, Boolean>build().asMap());;
    private static final AtomicInteger boxCount = new AtomicInteger(0);

    public static void drawBox(BlockPos pos, int colour) {
        drawBox(new AABB(pos), colour);
    }

    public static void drawBox(AABB box, int colour) {
        boxCount.getAndIncrement();
        Vec3 minPosition = box.getMinPosition();
        Vec3 size = box.getMaxPosition().subtract(box.getMinPosition());

        addLine(new Line(minPosition, minPosition.add(size.x(), 0, 0), colour));
        addLine(new Line(minPosition, minPosition.add(0, size.y(), 0), colour));
        addLine(new Line(minPosition, minPosition.add(0, 0, size.z()), colour));
        addLine(new Line(minPosition.add(size.x(), 0, size.z()), minPosition.add(size.x(), 0, 0), colour));
        addLine(new Line(minPosition.add(size.x(), 0, size.z()), minPosition.add(size.x(), size.y(), size.z()), colour));
        addLine(new Line(minPosition.add(size.x(), 0, size.z()), minPosition.add(0, 0, size.z()), colour));
        addLine(new Line(minPosition.add(size.x(), size.y(), 0), minPosition.add(size.x(), 0, 0), colour));
        addLine(new Line(minPosition.add(size.x(), size.y(), 0), minPosition.add(0, size.y(), 0), colour));
        addLine(new Line(minPosition.add(size.x(), size.y(), 0), minPosition.add(size.x(), size.y(), size.z()), colour));
        addLine(new Line(minPosition.add(0, size.y(), size.z()), minPosition.add(0, 0, size.z()), colour));
        addLine(new Line(minPosition.add(0, size.y(), size.z()), minPosition.add(0, size.y(), 0), colour));
        addLine(new Line(minPosition.add(0, size.y(), size.z()), minPosition.add(size.x(), size.y(), size.z()), colour));
    }

    public static int clear() {
        int size = boxCount.getAndSet(0);
        lines.clear();
        return size;
    }

    private static void addLine(Line line) {
        if (!lines.add(line)) {
            lines.remove(line);
        }
    }

    public static void registerEvents() {
        WorldRenderEvents.AFTER_ENTITIES.register(RenderManager::renderLines);
    }

    private static void renderLines(WorldRenderContext context) {
        lines.forEach(line -> {
            Line relativeLine = line.offset(context.camera().getPosition().scale(-1));
            Vec3 start = relativeLine.start();
            Vec3 end = relativeLine.end();
            Vec3 normal = end.subtract(start).normalize();
            int colour = line.colour();
            float red = ARGB.redFloat(colour);
            float green = ARGB.greenFloat(colour);
            float blue = ARGB.blueFloat(colour);

            PoseStack stack = context.matrixStack();
            stack.pushPose();
            PoseStack.Pose pose = stack.last();
            VertexConsumer buffer = context.consumers().getBuffer(NoDepthLayer.LINES_NO_DEPTH_LAYER);
            buffer
                .addVertex(pose, (float) start.x, (float) start.y, (float) start.z)
                .setColor(red, green, blue, 1.0F)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
            buffer
                .addVertex(pose, (float) end.x, (float) end.y, (float) end.z)
                .setColor(red, green, blue, 1.0F)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
            stack.popPose();
        });
    }
}
