package dev.xpple.seedmapper.mixin.baritone;

import baritone.behavior.PathingBehavior;
import dev.xpple.seedmapper.util.BaritoneIntegration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PathingBehavior.class, remap = false)
public class PathingBehaviorMixin {
    @Inject(method = "cancelEverything", at = @At("HEAD"), remap = false)
    private void onCancelEverything(CallbackInfoReturnable<Boolean> cir) {
        BaritoneIntegration.clearGoals();
    }
}
