package dev.xpple.seedmapper;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinConfigPlugin implements IMixinConfigPlugin {

    private static final Set<String> BARITONE_MIXINS = Set.of(
        "dev.xpple.seedmapper.mixin.baritone.CustomGoalProcessMixin",
        "dev.xpple.seedmapper.mixin.baritone.PathingBehaviorMixin"
    );

    private static final Set<String> DEV_ONLY_MIXINS = Set.of(
        "dev.xpple.seedmapper.mixin.RandomizableContainerMixin"
    );

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (BARITONE_MIXINS.contains(mixinClassName)) {
            return SeedMapper.BARITONE_AVAILABLE;
        }
        if (DEV_ONLY_MIXINS.contains(mixinClassName)) {
            return FabricLoader.getInstance().isDevelopmentEnvironment();
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
