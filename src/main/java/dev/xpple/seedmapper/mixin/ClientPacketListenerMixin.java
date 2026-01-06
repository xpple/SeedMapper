package dev.xpple.seedmapper.mixin;

import dev.xpple.seedmapper.SeedMapper;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.render.RenderManager;
import dev.xpple.seedmapper.seedmap.MinimapManager;
import dev.xpple.seedmapper.util.BaritoneIntegration;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V", shift = At.Shift.AFTER))
    private void onHandleLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
        RenderManager.clear();

        MinimapManager.hide();

        if (SeedMapper.BARITONE_AVAILABLE) {
            BaritoneIntegration.clearMinedBlocks();
        }
    }

    @Inject(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V", shift = At.Shift.AFTER))
    private void onHandleRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        RenderManager.clear();

        int dimension = CustomClientCommandSource.inferDimension(packet.commonPlayerSpawnInfo().dimensionType().value());
        MinimapManager.updateDimension(dimension);

        if (SeedMapper.BARITONE_AVAILABLE) {
            BaritoneIntegration.clearMinedBlocks();
        }
    }
}
