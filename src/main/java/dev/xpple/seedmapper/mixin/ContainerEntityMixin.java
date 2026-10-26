package dev.xpple.seedmapper.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerEntity.class)
public interface ContainerEntityMixin extends Container, MenuProvider {
    @Inject(method = "unpackChestVehicleLootTable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/ContainerEntity;setContainerLootTable(Lnet/minecraft/resources/ResourceKey;)V"))
    private void retainLootTableInfo(Player player, CallbackInfo ci) {
        if (!(this instanceof MinecartChest minecartChest)) {
            return;
        }
        ResourceKey<LootTable> lootTable = minecartChest.getContainerLootTable();
        if (lootTable == null) {
            return;
        }

        CustomData customData = minecartChest.get(DataComponents.CUSTOM_DATA);
        CompoundTag compoundTag = customData == null ? new CompoundTag() : customData.copyTag();
        Tag lootTableTag = Identifier.CODEC.encodeStart(NbtOps.INSTANCE, lootTable.identifier()).getOrThrow();
        compoundTag.put(RandomizableContainer.LOOT_TABLE_TAG, lootTableTag);
        compoundTag.putLong(RandomizableContainer.LOOT_TABLE_SEED_TAG, minecartChest.getContainerLootTableSeed());

        minecartChest.setComponent(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag));
    }
}
