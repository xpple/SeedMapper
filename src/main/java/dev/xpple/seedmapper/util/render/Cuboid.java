package dev.xpple.seedmapper.util.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class Cuboid extends Shape {

    private final Vec3d start;
    public final Vec3d size;
    private final int red;
    private final int green;
    private final int blue;

    public Cuboid(Box box, int color) {
        this(new Vec3d(box.minX, box.minY, box.minZ), new Vec3d(box.maxX, box.maxY, box.maxZ), color);
    }

    private Cuboid(Vec3d start, Vec3d end, int color) {
        this.start = start;
        this.size = new Vec3d(end.getX() - start.getX(), end.getY() - start.getY(), end.getZ() - start.getZ());
        this.red = (color >> 16) & 0xFF;
        this.green = (color >> 8) & 0xFF;
        this.blue = color & 0xFF;
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, float delta) {
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(OverlayRenderLayer.getOverlay());
        WorldRenderer.drawBox(matrixStack, vertexConsumer, 0, 0, 0, this.size.x, this.size.y, this.size.z, this.red / 255.0F, this.green / 255.0F, this.blue / 255.0F, 1.0F);
    }

    @Override
    public Vec3d getPos() {
        return this.start;
    }
}
