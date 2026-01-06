package dev.xpple.seedmapper.util;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
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
    private static final ConcurrentHashMap.KeySetView<BlockPos, Boolean> targetBlocks = ConcurrentHashMap.newKeySet();
    private static volatile @Nullable GoalBlock currentGoal = null;

    public static void addGoals(Collection<? extends Vec3i> blocks) {
        blocks.forEach(v -> targetBlocks.add(new BlockPos(v)));
        if (currentGoal == null) {
            setGoalForNextBlock();
        }
    }

    public static void onGoalCompletion(Goal goal) {
        if (currentGoal == null) {
            return;
        }
        // check goal equality by reference, this makes sure it was the goal from SeedMapper that was completed
        if (goal == currentGoal) {
            setGoalForNextBlock();
        }
    }

    public static void setGoalForNextBlock() {
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
        currentGoal = null;
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal(null);
    }
}
