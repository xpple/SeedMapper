package dev.xpple.seedmapper.mixin.seedmap;

import dev.xpple.seedmapper.seedmap.OptionsHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow @Final public Options options;

    @Shadow public LocalPlayer player;

    @Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;continueAttack(Z)V"))
    private void onKeySeedMap(CallbackInfo ci) {
        while (((OptionsHooks) this.options).seedMapper$getKeySeedMap().consumeClick()) {
            this.player.connection.sendCommand("sm:seedmap");
        }
    }
}
