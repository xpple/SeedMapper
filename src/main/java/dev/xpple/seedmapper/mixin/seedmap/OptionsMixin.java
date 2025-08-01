package dev.xpple.seedmapper.mixin.seedmap;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xpple.seedmapper.seedmap.OptionsHooks;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Debug(export = true)
@Mixin(Options.class)
public class OptionsMixin implements OptionsHooks {
    @Unique
    public final KeyMapping keySeedMap = new KeyMapping("key.seedMap", InputConstants.KEY_M, KeyMapping.CATEGORY_GAMEPLAY);

    @SuppressWarnings("unchecked")
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/ArrayUtils;addAll([Ljava/lang/Object;[Ljava/lang/Object;)[Ljava/lang/Object;", remap = false), index = 1)
    private <T> T[] addKeySeedMap(T[] array2) {
        return (T[]) ArrayUtils.add(array2, this.keySeedMap);
    }

    @Override
    public KeyMapping seedMapper$getKeySeedMap() {
        return keySeedMap;
    }
}
