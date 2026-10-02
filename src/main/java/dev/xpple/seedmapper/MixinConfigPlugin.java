package dev.xpple.seedmapper;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import java.util.Set;

public class MixinConfigPlugin implements IMixinConfigPlugin {

    public static final boolean BARITONE_AVAILABLE = FabricLoader.getInstance().getModContainer("baritone-meteor").isPresent();

    private static final Set<String> BARITONE_MIXINS = Set.of(
        "dev.xpple.seedmapper.mixin.baritone.CustomGoalProcessMixin",
        "dev.xpple.seedmapper.mixin.baritone.PathingBehaviorMixin"
    );

    private static final Set<String> DEV_ONLY_MIXINS = Set.of(
        "dev.xpple.seedmapper.mixin.RandomizableContainerMixin"
    );

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (BARITONE_MIXINS.contains(mixinClassName)) {
            return BARITONE_AVAILABLE;
        }
        if (DEV_ONLY_MIXINS.contains(mixinClassName)) {
            return FabricLoader.getInstance().isDevelopmentEnvironment();
        }
        return true;
    }
}
