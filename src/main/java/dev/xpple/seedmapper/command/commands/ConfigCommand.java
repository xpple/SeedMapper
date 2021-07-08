package dev.xpple.seedmapper.command.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.brigadier.Command;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.config.Config;
import net.minecraft.block.Block;
import net.minecraft.text.TranslatableText;

import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.LongArgumentType.getLong;
import static com.mojang.brigadier.arguments.LongArgumentType.longArg;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static dev.xpple.seedmapper.SeedMapper.CLIENT;
import static dev.xpple.seedmapper.command.arguments.BlockArgumentType.block;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;

public class ConfigCommand extends ClientCommand {

    @Override
    protected void register() {
        argumentBuilder
                .then(literal("automate")
                        .then(literal("set")
                                .then(argument("bool", bool())
                                        .executes(ctx -> setAutomate((CustomClientCommandSource) ctx.getSource(), getBool(ctx, "bool")))))
                        .then(literal("get")
                                .executes(ctx -> getAutomate((CustomClientCommandSource) ctx.getSource())))
                        .then(literal("toggle")
                                .executes(ctx -> toggleAutomate((CustomClientCommandSource) ctx.getSource()))))
                .then(literal("seed")
                        .then(literal("set")
                                .then(argument("seed", longArg())
                                        .executes(ctx -> setSeed((CustomClientCommandSource) ctx.getSource(), getLong(ctx, "seed")))))
                        .then(literal("get")
                                .executes(ctx -> getSeed((CustomClientCommandSource) ctx.getSource()))))
                .then(literal("seeds")
                        .then(literal("add")
                                .then(argument("seed", longArg())
                                        .executes(ctx -> addSeed((CustomClientCommandSource) ctx.getSource(), getLong(ctx, "seed")))))
                        .then(literal("remove")
                                .then(argument("key", string())
                                        .suggests(((context, builder) -> suggestMatching(Config.getSeeds().keySet().stream(), builder)))
                                        .executes(ctx -> removeSeed((CustomClientCommandSource) ctx.getSource(), getString(ctx, "key"))))))
                .then(literal("ignored")
                        .then(literal("add")
                                .then(argument("block", block())
                                        .executes(ctx -> addBlock((CustomClientCommandSource) ctx.getSource(), ctx.getArgument("block", Block.class)))))
                        .then(literal("remove")
                                .then(argument("block", block())
                                        .suggests(((context, builder) -> suggestMatching(Config.getIgnoredBlocks().stream().map(block -> block.getName().getString()), builder)))
                                        .executes(ctx -> removeBlock((CustomClientCommandSource) ctx.getSource(), ctx.getArgument("block", Block.class)))))
                        .then(literal("list")
                                .executes(ctx -> listBlocks((CustomClientCommandSource) ctx.getSource()))));
    }

    @Override
    protected String rootLiteral() {
        return "config";
    }

    /*
    If automation is set to true, new chunks will automatically be compared.
    */
    private int setAutomate(CustomClientCommandSource source, boolean value) {
        Config.toggle("automate", value);
        Chat.print("", new TranslatableText("command.config.setAutomate", value));
        return Command.SINGLE_SUCCESS;
    }

    private int getAutomate(CustomClientCommandSource source) {
        Chat.print("", new TranslatableText("command.config.getAutomate", Config.isEnabled("automate")));
        return Command.SINGLE_SUCCESS;
    }

    private int toggleAutomate(CustomClientCommandSource source) {
        boolean old = Config.isEnabled("automate");
        Config.toggle("automate", !old);
        Chat.print("", new TranslatableText("command.config.toggleAutomate", !old));
        return Command.SINGLE_SUCCESS;
    }

    /*
    The seed that will be used for the comparison.
    */
    private int setSeed(CustomClientCommandSource source, long seed) {
        Config.set("seed", seed);
        Chat.print("", new TranslatableText("command.config.setSeed", seed));
        return Command.SINGLE_SUCCESS;
    }

    private int getSeed(CustomClientCommandSource source) {
        JsonElement element = Config.get("seed");
        if (element instanceof JsonNull) {
            Chat.print("", new TranslatableText("command.config.getSeed.null"));
        } else {
            Chat.print("", new TranslatableText("command.config.getSeed", element.getAsLong()));
        }
        return Command.SINGLE_SUCCESS;
    }

    /*
    If seeds are added to this list, they will be used if the server matches the key.
    `CLIENT.getNetworkHandler().getConnection().getAddress().toString()` will be used to compare the key.
     */
    private int addSeed(CustomClientCommandSource source, long seed) {
        String key = CLIENT.getNetworkHandler().getConnection().getAddress().toString();
        if (Config.addSeed(key, seed)) {
            Chat.print("", new TranslatableText("command.config.addSeed.success"));
        } else {
            Chat.print("", new TranslatableText("command.config.addSeed.alreadyAdded"));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int removeSeed(CustomClientCommandSource source, String key) {
        if (Config.removeSeed(key)) {
            Chat.print("", new TranslatableText("command.config.removeSeed.success"));
        } else {
            Chat.print("", new TranslatableText("command.config.removeSeed.notAdded"));
        }
        return Command.SINGLE_SUCCESS;
    }

    /*
    If blocks are added to this list, they will be ignored in the overlay process.
    */
    private int addBlock(CustomClientCommandSource source, Block block) {
        if (Config.addBlock(block)) {
            Chat.print("", new TranslatableText("command.config.addBlock.success", block.getName()));
        } else {
            Chat.print("", new TranslatableText("command.config.addBlock.alreadyIgnored", block.getName()));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int removeBlock(CustomClientCommandSource source, Block block) {
        if (Config.removeBlock(block)) {
            Chat.print("", new TranslatableText("command.config.removeBlock.success", block.getName()));
        } else {
            Chat.print("", new TranslatableText("command.config.removeBlock.notIgnored", block.getName()));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int listBlocks(CustomClientCommandSource source) {
        if (Config.getIgnoredBlocks().isEmpty()) {
            Chat.print("", new TranslatableText("command.config.listBlocks.empty"));
        } else {
            Chat.print("", new TranslatableText("command.config.listBlocks", Config.getIgnoredBlocks().stream().map(block -> block.getName().getString()).collect(Collectors.joining(", "))));
        }
        return Command.SINGLE_SUCCESS;
    }
}
