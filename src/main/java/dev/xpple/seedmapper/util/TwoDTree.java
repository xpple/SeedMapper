package dev.xpple.seedmapper.util;

import net.minecraft.core.BlockPos;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class TwoDTree implements Iterable<BlockPos> {
    private @Nullable Node root;

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

    public @Nullable BlockPos nearestTo(BlockPos target) {
        if (this.root == null) {
            return null;
        }
        return this.nearestTo(this.root, target, true, this.root.pos, Double.POSITIVE_INFINITY).pos;
    }

    public void balance() {
        if (this.root == null) {
            return;
        }

        List<BlockPos> points = new ArrayList<>();
        for (BlockPos pos : this) {
            points.add(pos);
        }

        this.root = buildBalanced(points, true);
    }

    public void clear() {
        this.root = null;
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

    private Node buildBalanced(List<BlockPos> points, boolean x) {
        if (points.isEmpty()) {
            return null;
        }

        points.sort(x ? Comparator.comparingInt(BlockPos::getX) : Comparator.comparingInt(BlockPos::getZ));

        int mid = points.size() / 2;
        BlockPos median = points.get(mid);

        Node node = new Node(median);

        node.left = buildBalanced(points.subList(0, mid), !x);
        node.right = buildBalanced(points.subList(mid + 1, points.size()), !x);

        return node;
    }

    @Override
    public @NonNull Iterator<BlockPos> iterator() {
        return new Iterator<>() {
            private final Stack<Node> stack = new Stack<>();

            {
                this.pushLeft(root);
            }

            private void pushLeft(Node node) {
                while (node != null) {
                    this.stack.push(node);
                    node = node.left;
                }
            }

            @Override
            public boolean hasNext() {
                return !this.stack.isEmpty();
            }

            @Override
            public BlockPos next() {
                Node current = this.stack.pop();
                BlockPos result = current.pos;
                if (current.right != null) {
                    this.pushLeft(current.right);
                }
                return result;
            }
        };
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
