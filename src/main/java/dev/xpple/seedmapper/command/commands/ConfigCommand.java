package dev.xpple.seedmapper.command.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.config.Config;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.text.TranslatableText;

import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.LongArgumentType.getLong;
import static com.mojang.brigadier.arguments.LongArgumentType.longArg;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static dev.xpple.seedmapper.SeedMapper.CLIENT;
import static dev.xpple.seedmapper.command.arguments.BlockArgumentType.block;
import static dev.xpple.seedmapper.command.arguments.BlockArgumentType.getBlock;
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
                                        .executes(this::setAutomate)))
                        .then(literal("get")
                                .executes(this::getAutomate))
                        .then(literal("toggle")
                                .executes(this::toggleAutomate)))
                .then(literal("seed")
                        .then(literal("set")
                                .then(argument("seed", longArg())
                                        .executes(this::setSeed)))
                        .then(literal("get")
                                .executes(this::getSeed)))
                .then(literal("seeds")
                        .then(literal("add")
                                .then(argument("seed", longArg())
                                        .executes(this::addSeed)))
                        .then(literal("remove")
                                .then(argument("key", greedyString())
                                        .suggests(((context, builder) -> suggestMatching(Config.getSeeds().keySet().stream(), builder)))
                                        .executes(this::removeSeed))))
                .then(literal("ignored")
                        .then(literal("add")
                                .then(argument("block", block())
                                        .executes(this::addBlock)))
                        .then(literal("remove")
                                .then(argument("block", block())
                                        .suggests(((context, builder) -> suggestMatching(Config.getIgnoredBlocks().stream().map(block -> block.getName().getString()), builder)))
                                        .executes(this::removeBlock)))
                        .then(literal("list")
                                .executes(this::listBlocks)));
    }

    @Override
    protected String rootLiteral() {
        return "config";
    }

    /*
    If automation is set to true, new chunks will automatically be compared.
    */
    private int setAutomate(CommandContext<FabricClientCommandSource> ctx) {
        boolean value = getBool(ctx, "bool");
        Config.toggle("automate", value);
        Chat.print("", new TranslatableText("command.config.setAutomate", value));
        return Command.SINGLE_SUCCESS;
    }

    private int getAutomate(CommandContext<FabricClientCommandSource> ctx) {
        Chat.print("", new TranslatableText("command.config.getAutomate", Config.isEnabled("automate")));
        return Command.SINGLE_SUCCESS;
    }

    private int toggleAutomate(CommandContext<FabricClientCommandSource> ctx) {
        boolean old = Config.isEnabled("automate");
        Config.toggle("automate", !old);
        Chat.print("", new TranslatableText("command.config.toggleAutomate", !old));
        return Command.SINGLE_SUCCESS;
    }

    /*
    The seed that will be used for the comparison.
    */
    private int setSeed(CommandContext<FabricClientCommandSource> ctx) {
        long seed = getLong(ctx, "seed");
        Config.set("seed", seed);
        Chat.print("", new TranslatableText("command.config.setSeed", seed));
        return Command.SINGLE_SUCCESS;
    }

    private int getSeed(CommandContext<FabricClientCommandSource> ctx) {
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
    private int addSeed(CommandContext<FabricClientCommandSource> ctx) {
        String key = CLIENT.getNetworkHandler().getConnection().getAddress().toString();
        long seed = getLong(ctx, "seed");
        if (Config.addSeed(key, seed)) {
            Chat.print("", new TranslatableText("command.config.addSeed.success"));
        } else {
            Chat.print("", new TranslatableText("command.config.addSeed.alreadyAdded"));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int removeSeed(CommandContext<FabricClientCommandSource> ctx) {
        String key = getString(ctx, "key");
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
    private int addBlock(CommandContext<FabricClientCommandSource> ctx) {
        Block block = getBlock(ctx, "block");
        if (Config.addBlock(block)) {
            Chat.print("", new TranslatableText("command.config.addBlock.success", block.getName()));
        } else {
            Chat.print("", new TranslatableText("command.config.addBlock.alreadyIgnored", block.getName()));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int removeBlock(CommandContext<FabricClientCommandSource> ctx) {
        Block block = getBlock(ctx, "block");
        if (Config.removeBlock(block)) {
            Chat.print("", new TranslatableText("command.config.removeBlock.success", block.getName()));
        } else {
            Chat.print("", new TranslatableText("command.config.removeBlock.notIgnored", block.getName()));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int listBlocks(CommandContext<FabricClientCommandSource> ctx) {
        if (Config.getIgnoredBlocks().isEmpty()) {
            Chat.print("", new TranslatableText("command.config.listBlocks.empty"));
        } else {
            Chat.print("", new TranslatableText("command.config.listBlocks", Config.getIgnoredBlocks().stream().map(block -> block.getName().getString()).collect(Collectors.joining(", "))));
        }
        return Command.SINGLE_SUCCESS;
    }
}
