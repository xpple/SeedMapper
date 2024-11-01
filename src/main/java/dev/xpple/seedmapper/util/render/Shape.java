package dev.xpple.seedmapper.util.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

public abstract class Shape {
    int deathTime;
    protected Vec3 prevPos;

    public void tick() {
    }

    public abstract void render(PoseStack stack, MultiBufferSource bufferSource, float delta);

    public abstract Vec3 getPos();
}
