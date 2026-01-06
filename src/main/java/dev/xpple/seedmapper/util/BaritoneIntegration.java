package dev.xpple.seedmapper.util;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/*
 * This class should NOT be loaded if Baritone is not available.
 */
public class BaritoneIntegration {
    /*
     * There probably exists a more efficient data structure, but I couldn't find one that really appealed to me.
     * Basically, you want a data structure that implements fast nearest-neighbour searches with popping (i.e. find the
     * closest block to a given input block and remove it). Particularly the removing aspect is limiting, and for
     * instance rules out 3D-trees. I figured for a not too large number of blocks, a hash set suffices.
     */
    private static final ConcurrentHashMap.KeySetView<BlockPos, ?> targetBlocks = ConcurrentHashMap.newKeySet();
    private static @Nullable GoalBlock currentGoal = null;
    private static final Object CURRENT_GOAL_LOCK = new Object();

    private static final ConcurrentHashMap.KeySetView<BlockPos, ?> minedBlocks = ConcurrentHashMap.newKeySet();

    public static void addGoals(List<BlockPos> blocks) {
        blocks.stream()
            .filter(v -> !minedBlocks.contains(v))
            .forEach(targetBlocks::add);

        synchronized (CURRENT_GOAL_LOCK) {
            if (currentGoal == null) {
                setGoalForNextBlock();
            }
        }
    }

    public static void onGoalCompletion(Goal goal) {
        if (currentGoal == null) {
            return;
        }

        // check goal equality by reference, this makes sure it was the goal from SeedMapper that was completed
        synchronized (CURRENT_GOAL_LOCK) {
            if (goal == currentGoal) {
                minedBlocks.add(currentGoal.getGoalPos());
                setGoalForNextBlock();
            }
        }
    }

    private static void setGoalForNextBlock() {
        if (targetBlocks.isEmpty()) {
            currentGoal = null;
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Vec3 playerPos = player.position();
        BlockPos closest = targetBlocks.stream().min(Comparator.comparingDouble(b -> b.distToLowCornerSqr(playerPos.x, playerPos.y, playerPos.z))).orElseThrow();
        targetBlocks.remove(closest);
        currentGoal = new GoalBlock(closest);
        // delay configuring the goal until the next tick, otherwise the new goal won't be executed
        Minecraft.getInstance().schedule(() -> BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(currentGoal));
    }

    public static void clearGoals() {
        targetBlocks.clear();
        synchronized (CURRENT_GOAL_LOCK) {
            currentGoal = null;
        }
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal(null);
    }

    public static void clearMinedBlocks() {
        minedBlocks.clear();
    }
}
