package dev.xpple.seedmapper.command.arguments;

import com.mojang.brigadier.context.CommandContext;
import dev.xpple.betterconfig.util.WrappedArgumentType;
import dev.xpple.seedmapper.config.ColorWrapper;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.arguments.HexColorArgument;

public class ColorWrapperArgument extends WrappedArgumentType.Converted<ColorWrapper, Integer> {
    private ColorWrapperArgument() {
        super(HexColorArgument.hexColor());
    }

    public static ColorWrapperArgument colorWrapper() {
        return new ColorWrapperArgument();
    }

    public static ColorWrapper getBlock(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, ColorWrapper.class);
    }

    @Override
    public ColorWrapper convert(Integer nativeType) {
        return new ColorWrapper(nativeType);
    }
}
