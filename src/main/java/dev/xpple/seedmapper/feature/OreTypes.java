package dev.xpple.seedmapper.feature;

import com.github.cubiomes.Cubiomes;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

public final class OreTypes {
    private OreTypes() {
    }

    public static final Set<Integer> ORE_TYPES = ImmutableSet.<Integer>builder()
        .add(Cubiomes.AndesiteOre())
        .add(Cubiomes.BlackstoneOre())
        .add(Cubiomes.BuriedDiamondOre())
        .add(Cubiomes.BuriedLapisOre())
        .add(Cubiomes.ClayOre())
        .add(Cubiomes.CoalOre())
        .add(Cubiomes.CopperOre())
        .add(Cubiomes.DeepslateOre())
        .add(Cubiomes.DeltasGoldOre())
        .add(Cubiomes.DeltasQuartzOre())
        .add(Cubiomes.DiamondOre())
        .add(Cubiomes.DioriteOre())
        .add(Cubiomes.DirtOre())
        .add(Cubiomes.EmeraldOre())
        .add(Cubiomes.ExtraGoldOre())
        .add(Cubiomes.GoldOre())
        .add(Cubiomes.GraniteOre())
        .add(Cubiomes.GravelOre())
        .add(Cubiomes.IronOre())
        .add(Cubiomes.LapisOre())
        .add(Cubiomes.LargeCopperOre())
        .add(Cubiomes.LargeDebrisOre())
        .add(Cubiomes.LargeDiamondOre())
        .add(Cubiomes.LowerAndesiteOre())
        .add(Cubiomes.LowerCoalOre())
        .add(Cubiomes.LowerDioriteOre())
        .add(Cubiomes.LowerGoldOre())
        .add(Cubiomes.LowerGraniteOre())
        .add(Cubiomes.LowerRedstoneOre())
        .add(Cubiomes.MagmaOre())
        .add(Cubiomes.MediumDiamondOre())
        .add(Cubiomes.MiddleIronOre())
        .add(Cubiomes.NetherGoldOre())
        .add(Cubiomes.NetherGravelOre())
        .add(Cubiomes.NetherQuartzOre())
        .add(Cubiomes.RedstoneOre())
        .add(Cubiomes.SmallDebrisOre())
        .add(Cubiomes.SmallIronOre())
        .add(Cubiomes.SoulSandOre())
        .add(Cubiomes.TuffOre())
        .add(Cubiomes.UpperAndesiteOre())
        .add(Cubiomes.UpperCoalOre())
        .add(Cubiomes.UpperDioriteOre())
        .add(Cubiomes.UpperGraniteOre())
        .add(Cubiomes.UpperIronOre())
        .build();
}
