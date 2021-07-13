package dev.xpple.seedmapper.mixin.command;

import net.minecraft.command.argument.CoordinateArgument;
import net.minecraft.command.argument.DefaultPosArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DefaultPosArgument.class)
public interface DefaultPosArgumentAccessor {

    @Accessor("x")
    CoordinateArgument getX();

    @Accessor("y")
    CoordinateArgument getY();

    @Accessor("z")
    CoordinateArgument getZ();
}
