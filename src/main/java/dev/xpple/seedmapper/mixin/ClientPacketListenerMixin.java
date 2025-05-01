package dev.xpple.seedmapper.mixin;

import dev.xpple.seedmapper.render.RenderManager;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleLogin", at = @At("HEAD"))
    private void onHandleLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
        RenderManager.clear();
    }

    @Inject(method = "handleRespawn", at = @At("HEAD"))
    private void onHandleRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        RenderManager.clear();
    }
}
