package dev.xpple.seedmapper.util;

import net.minecraft.core.BlockPos;

public class TwoDTree {
    private Node root;

    public boolean isEmpty() {
        return this.root == null;
    }

    public void insert(BlockPos pos) {
        this.root = this.insert(this.root, pos, true);
    }

    private Node insert(Node node, BlockPos pos, boolean x) {
        if (node == null) {
            return new Node(pos);
        }

        if ((x && pos.getX() < node.pos.getX()) || (!x && pos.getZ() < node.pos.getZ())) {
            node.left = this.insert(node.left, pos, !x);
        } else {
            node.right = this.insert(node.right, pos, !x);
        }

        return node;
    }

    public BlockPos nearestTo(BlockPos target) {
        if (this.root == null) {
            return null;
        }
        return this.nearestTo(this.root, target, true, this.root.pos, Double.POSITIVE_INFINITY).pos;
    }

    private Result nearestTo(Node node, BlockPos target, boolean x, BlockPos bestPos, double bestDistanceSqr) {
        if (node == null) {
            return new Result(bestPos, bestDistanceSqr);
        }

        double currentDistanceSqr = target.distToLowCornerSqr(node.pos.getX(), 0, node.pos.getZ());
        if (currentDistanceSqr < bestDistanceSqr) {
            bestDistanceSqr = currentDistanceSqr;
            bestPos = node.pos;
        }

        Node near, far;
        if ((x && target.getX() < node.pos.getX()) || (!x && target.getZ() < node.pos.getZ())) {
            near = node.left;
            far = node.right;
        } else {
            near = node.right;
            far = node.left;
        }

        Result bestResult = this.nearestTo(near, target, !x, bestPos, bestDistanceSqr);
        bestPos = bestResult.pos;
        bestDistanceSqr = bestResult.distance;

        int distance = x ? target.getX() - node.pos.getX() : target.getZ() - node.pos.getZ();
        if (distance * distance < bestDistanceSqr) {
            Result farResult = this.nearestTo(far, target, !x, bestPos, bestDistanceSqr);
            if (farResult.distance < bestDistanceSqr) {
                bestResult = farResult;
            }
        }

        return bestResult;
    }

    private static class Node {
        private final BlockPos pos;
        public Node left;
        public Node right;

        public Node(BlockPos pos) {
            this.pos = pos;
        }
    }

    private record Result(BlockPos pos, double distance) {
    }
}
