package dev.xpple.seedmapper.mixin;

import dev.xpple.seedmapper.render.EndMainPassEvent;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "lambda$addMainPass$2", at = @At(value = "INVOKE:LAST", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"))
    private void onEndMainPass(CallbackInfo ci) {
        EndMainPassEvent.END_MAIN_PASS.invoker().endMainPass(WorldRenderContext.getInstance(LevelRenderer.class.cast(this)));
    }
}
