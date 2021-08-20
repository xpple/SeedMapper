package dev.xpple.seedmapper.mixin.client;

import dev.xpple.seedmapper.SeedMapper;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "onCommandTree", at = @At("TAIL"))
    private void registerCommands(CommandTreeS2CPacket packet, CallbackInfo ci) {
        SeedMapper.registerCommands(ClientCommandManager.DISPATCHER);
    }
}
