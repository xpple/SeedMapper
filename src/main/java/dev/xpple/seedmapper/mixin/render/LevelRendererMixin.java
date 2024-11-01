package dev.xpple.seedmapper.mixin.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xpple.seedmapper.util.render.RenderQueue;
import dev.xpple.seedmapper.util.render.Shape;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow @Final private RenderBuffers renderBuffers;

    @Shadow private @Nullable ClientLevel level;

    @Inject(method = "lambda$addMainPass$1", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=translucent", shift = At.Shift.AFTER))
    private void render(FogParameters fogParameters, DeltaTracker deltaTracker, Camera camera, ProfilerFiller profilerFiller, Matrix4f matrix4f, Matrix4f matrix4f2, ResourceHandle<?> resourceHandle, ResourceHandle<?> resourceHandle2, ResourceHandle<?> resourceHandle3, ResourceHandle<?> resourceHandle4, boolean bl, Frustum frustum, ResourceHandle<?> resourceHandle5, CallbackInfo ci, @Local PoseStack poseStack) {
        Map<Object, Shape> shapeMap = RenderQueue.queue.get(RenderQueue.Layer.ON_TOP);
        if (shapeMap == null) {
            return;
        }

        Vec3 cameraPos = camera.getPosition();
        Map<Object, Shape> tempMap = new HashMap<>(shapeMap);
        tempMap.values().forEach(shape -> {
            Vec3 pos = shape.getPos();
            if (!this.level.getChunkSource().hasChunk(Mth.floor(pos.x) >> 4, Mth.floor(pos.z) >> 4)) {
                shapeMap.remove(shape);
                return;
            }
            poseStack.pushPose();
            poseStack.translate(pos.x - cameraPos.x, pos.y - cameraPos.y, pos.z - cameraPos.z);
            shape.render(poseStack, this.renderBuffers.outlineBufferSource(), deltaTracker.getRealtimeDeltaTicks());
            poseStack.popPose();
        });
    }
}
