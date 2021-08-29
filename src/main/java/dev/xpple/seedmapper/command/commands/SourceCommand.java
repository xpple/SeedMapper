package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.xpple.clientarguments.arguments.CDimensionArgumentType;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import kaptainwutax.mcutils.version.MCVersion;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.util.Identifier;

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
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.*;

public class SourceCommand extends ClientCommand {

    @Override
    protected void build() {
        LiteralCommandNode<FabricClientCommandSource> root = DISPATCHER.register(literal("source"));
        argumentBuilder
                .then(literal("run")
                        .redirect(DISPATCHER.getRoot(), CommandContext::getSource))
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
                                .redirect(root, ctx -> CustomClientCommandSource.of(ctx.getSource()).withMeta("version", getMcVersion(ctx, "version")))));
    }

    @Override
    protected String rootLiteral() {
        return "source";
    }
}
