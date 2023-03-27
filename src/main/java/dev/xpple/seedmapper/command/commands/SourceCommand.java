package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.xpple.clientarguments.arguments.CDimensionArgumentType;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.util.Identifier;

import static com.mojang.brigadier.arguments.LongArgumentType.getLong;
import static com.mojang.brigadier.arguments.LongArgumentType.longArg;
import static dev.xpple.clientarguments.arguments.CDimensionArgumentType.dimension;
import static dev.xpple.clientarguments.arguments.CDimensionArgumentType.getCDimensionArgument;
import static dev.xpple.clientarguments.arguments.CEntityArgumentType.entity;
import static dev.xpple.clientarguments.arguments.CEntityArgumentType.getCEntity;
import static dev.xpple.clientarguments.arguments.CRotationArgumentType.getCRotation;
import static dev.xpple.clientarguments.arguments.CRotationArgumentType.rotation;
import static dev.xpple.clientarguments.arguments.CVec3ArgumentType.getCVec3;
import static dev.xpple.clientarguments.arguments.CVec3ArgumentType.vec3;
import static dev.xpple.seedmapper.command.arguments.MCVersionArgumentType.getMcVersion;
import static dev.xpple.seedmapper.command.arguments.MCVersionArgumentType.mcVersion;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class SourceCommand extends ClientCommand {

    @Override
    protected LiteralCommandNode<FabricClientCommandSource> build(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        LiteralArgumentBuilder<FabricClientCommandSource> rootLiteral = literal(this.getRootLiteral());
        LiteralCommandNode<FabricClientCommandSource> root = dispatcher.register(rootLiteral);
        return dispatcher.register(rootLiteral
            .then(literal("run")
                .redirect(dispatcher.getRoot(), CommandContext::getSource))
            .then(literal("as")
                .then(argument("entity", entity())
                    .redirect(root, ctx -> CustomClientCommandSource.of(ctx.getSource()).withEntity(getCEntity(ctx, "entity")))))
            .then(literal("positioned")
                .then(argument("pos", vec3())
                    .redirect(root, ctx -> CustomClientCommandSource.of(ctx.getSource()).withPosition(getCVec3(ctx, "pos")))))
            .then(literal("rotated")
                .then(argument("rot", rotation())
                    .redirect(root, ctx -> CustomClientCommandSource.of(ctx.getSource()).withRotation(getCRotation(ctx, "rot").toAbsoluteRotation(ctx.getSource())))))
            .then(literal("in")
                .then(argument("dimension", dimension())
                    .redirect(root, ctx -> {
                        CDimensionArgumentType.DimensionArgument dimensionArgument = getCDimensionArgument(ctx, "dimension");
                        return CustomClientCommandSource.of(ctx.getSource()).withMeta("dimension", new Identifier(dimensionArgument.getName()));
                    })))
            .then(literal("versioned")
                .then(argument("version", mcVersion().all())
                    .redirect(root, ctx -> CustomClientCommandSource.of(ctx.getSource()).withMeta("version", getMcVersion(ctx, "version")))))
            .then(literal("seeded")
                .then(argument("seed", longArg())
                    .redirect(root, ctx -> CustomClientCommandSource.of(ctx.getSource()).withMeta("seed", getLong(ctx, "seed"))))));
    }

    @Override
    protected String rootLiteral() {
        return "source";
    }
}
