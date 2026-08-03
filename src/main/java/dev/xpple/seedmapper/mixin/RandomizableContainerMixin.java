package dev.xpple.seedmapper.mixin;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// retain loot table information after loot has been generated
@Mixin(RandomizableContainer.class)
public interface RandomizableContainerMixin {
    // target right before `this.setLootTable(null)`
    @Inject(method = "unpackLootTable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/RandomizableContainer;setLootTable(Lnet/minecraft/resources/ResourceKey;)V"))
    private void retainLootTableInfo(Player player, CallbackInfo ci) {
        if (this instanceof RandomizableContainerBlockEntity randomizableContainerBlockEntity) {
            CompoundTag compoundTag = new CompoundTag();
            Tag lootTableTag = Identifier.CODEC.encodeStart(NbtOps.INSTANCE, randomizableContainerBlockEntity.getLootTable().identifier()).getOrThrow();
            compoundTag.put(RandomizableContainer.LOOT_TABLE_TAG, lootTableTag);
            compoundTag.putLong(RandomizableContainer.LOOT_TABLE_SEED_TAG, randomizableContainerBlockEntity.getLootTableSeed());
            DataComponentMap components = DataComponentMap.builder()
                .addAll(randomizableContainerBlockEntity.components())
                .set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag))
                .build();
            randomizableContainerBlockEntity.setComponents(components);
        }
    }
}
