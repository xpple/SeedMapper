package dev.xpple.seedmapper.mixin.render;

import dev.xpple.seedmapper.util.render.RenderQueue;
import dev.xpple.seedmapper.util.render.Shape;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

import static dev.xpple.seedmapper.SeedMapper.CLIENT;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Shadow @Final private BufferBuilderStorage bufferBuilders;

    @Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;crosshairTarget:Lnet/minecraft/util/hit/HitResult;", ordinal = 1))
    private void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        Map<Object, Shape> shapeMap = RenderQueue.queue.get(RenderQueue.Layer.ON_TOP);
        if (shapeMap == null) {
            return;
        }

        Vec3d cameraPos = camera.getPos();
        Map<Object, Shape> tempMap = new HashMap<>(shapeMap);
        tempMap.values().forEach(shape -> {
            Vec3d pos = shape.getPos();
            if (!CLIENT.world.getChunkManager().isChunkLoaded(MathHelper.floor(pos.x) >> 4, MathHelper.floor(pos.z) >> 4)) {
                shapeMap.remove(shape);
                return;
            }
            matrices.push();
            matrices.translate(pos.x - cameraPos.x, pos.y - cameraPos.y, pos.z - cameraPos.z);
            shape.render(matrices, this.bufferBuilders.getOutlineVertexConsumers(), tickDelta);
            matrices.pop();
        });
    }
}
