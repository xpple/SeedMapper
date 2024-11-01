package dev.xpple.seedmapper.util.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Cuboid extends Shape {

    private final Vec3 start;
    public final Vec3 size;
    private final int red;
    private final int green;
    private final int blue;

    public Cuboid(AABB box, int color) {
        this(new Vec3(box.minX, box.minY, box.minZ), new Vec3(box.maxX, box.maxY, box.maxZ), color);
    }

    private Cuboid(Vec3 start, Vec3 end, int color) {
        this.start = start;
        this.size = new Vec3(end.x() - start.x(), end.y() - start.y(), end.z() - start.z());
        this.red = (color >> 16) & 0xFF;
        this.green = (color >> 8) & 0xFF;
        this.blue = color & 0xFF;
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource bufferSource, float delta) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(OverlayRenderLayer.getOverlay());
        ShapeRenderer.renderLineBox(stack, vertexConsumer, 0, 0, 0, this.size.x, this.size.y, this.size.z, this.red / 255.0F, this.green / 255.0F, this.blue / 255.0F, 1.0F);
    }

    @Override
    public Vec3 getPos() {
        return this.start;
    }
}
