package dev.xpple.seedmapper.mixin.baritone;

import baritone.Baritone;
import baritone.api.pathing.goals.Goal;
import baritone.api.process.ICustomGoalProcess;
import baritone.process.CustomGoalProcess;
import baritone.utils.BaritoneProcessHelper;
import dev.xpple.seedmapper.SeedMapper;
import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.util.BaritoneIntegration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CustomGoalProcess.class, remap = false)
public abstract class CustomGoalProcessMixin extends BaritoneProcessHelper implements ICustomGoalProcess {
    @Shadow private Goal a;

    public CustomGoalProcessMixin(Baritone baritone) {
        super(baritone);
    }

    @Inject(method = "onLostControl", at = @At("HEAD"), remap = false)
    private void onFinished(CallbackInfo ci) {
        if (SeedMapper.BARITONE_AVAILABLE && Configs.AutoMine) {
            BaritoneIntegration.onGoalCompletion(this.a);
        }
    }
}
