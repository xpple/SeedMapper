package dev.xpple.seedmapper.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.xpple.seedmapper.SeedMapper;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.command.commands.VaultCommand;
import dev.xpple.seedmapper.render.RenderManager;
import dev.xpple.seedmapper.seedmap.MinimapManager;
import dev.xpple.seedmapper.util.BaritoneIntegration;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Unique
    private @Nullable BlockPos vaultEjectingPos;

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

    @Inject(method = "handleBlockUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V", shift = At.Shift.AFTER))
    private void onHandleBlockUpdate(ClientboundBlockUpdatePacket packet, CallbackInfo ci) {
        if (VaultCommand.predictor == null) {
            return;
        }
        Optional<VaultState> state = packet.getBlockState().getOptionalValue(VaultBlock.STATE);
        if (state.isEmpty()) {
            return;
        }

        boolean isOminous = packet.getBlockState().getValue(VaultBlock.OMINOUS);

        switch (state.get()) {
            case EJECTING -> {
                if (this.vaultEjectingPos == null) {
                    this.vaultEjectingPos = packet.getPos();
                }
            }
            case ACTIVE, INACTIVE -> {
                if (this.vaultEjectingPos != null && this.vaultEjectingPos.equals(packet.getPos())) {
                    this.vaultEjectingPos = null;
                    // this should always be true
                    if (VaultCommand.predictor.getEjectedItemIds().isEmpty()) {
                        VaultCommand.predictor.predictLoot(isOminous);
                        VaultCommand.predictor = null;
                    }
                }
            }
        }
    }

    @Inject(method = "handleAddEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;recreateFromPacket(Lnet/minecraft/network/protocol/game/ClientboundAddEntityPacket;)V", shift = At.Shift.AFTER))
    private void onHandleAddEntity(ClientboundAddEntityPacket packet, CallbackInfo ci, @Local(name = "entity") Entity entity) {
        if (VaultCommand.predictor == null) {
            return;
        }
        if (this.vaultEjectingPos == null) {
            return;
        }
        if (!(entity instanceof ItemEntity itemEntity)) {
            return;
        }
        if (new AABB(this.vaultEjectingPos).inflate(1).contains(itemEntity.position())) {
            VaultCommand.predictor.getEjectedItemIds().add(itemEntity.getId());
        }
    }

    @Inject(method = "handleSetEntityData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SynchedEntityData;assignValues(Ljava/util/List;)V", shift = At.Shift.AFTER))
    private void onHandleSetEntityData(ClientboundSetEntityDataPacket packet, CallbackInfo ci, @Local(name = "entity") Entity entity) {
        if (VaultCommand.predictor == null) {
            return;
        }
        if (!(entity instanceof ItemEntity itemEntity)) {
            return;
        }
        if (VaultCommand.predictor.getEjectedItemIds().remove(packet.id())) {
            VaultCommand.predictor.getEjectedItems().add(itemEntity.getItem().copy());
        }
    }
}
