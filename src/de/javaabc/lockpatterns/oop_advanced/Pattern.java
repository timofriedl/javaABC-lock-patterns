package de.javaabc.lockpatterns.oop_advanced;

import de.javaabc.lockpatterns.util.Cache;
import de.javaabc.lockpatterns.util.MathUtil;

import java.awt.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Pattern {
    // A cache to store the result of the nodesBetween() function
    private static final Cache<Set<Node>> NODES_BETWEEN_CACHE = new Cache<>();

    // A cache to store the result of the countValidPatterns() function
    private static final Cache<Long> COUNT_VALID_PATTERNS_CACHE = new Cache<>();

    // The last node of this pattern
    private final Node lastNode;

    // The set of nodes that have not been used in this pattern so far
    private final Set<Node> unusedNodes;

    /**
     * Creates a new pattern.
     *
     * @param lastNode    the last node of this pattern
     * @param unusedNodes the set of nodes that have not been used in this pattern so far
     */
    private Pattern(Node lastNode, Set<Node> unusedNodes) {
        this.lastNode = lastNode;
        this.unusedNodes = unusedNodes;
    }

    /**
     * Creates the set of all possible nodes in a given (size x size) grid
     *
     * @param size the width and height of the pattern grid
     * @return a {@link Set} of all possible nodes
     */
    private static Set<Node> allNodes(int size) {
        Set<Node> allNodes = new HashSet<>();
        for (int y = 0; y < size; y++)
            for (int x = 0; x < size; x++)
                allNodes.add(Node.at(y, x));
        return allNodes;
    }

    /**
     * Creates the empty pattern.
     *
     * @param size the width and height of the pattern grid
     */
    public Pattern(int size) {
        this(null, allNodes(size));
    }

    /**
     * Computes all {@link Node}s that lie exactly on a straight line that connects two given nodes.
     *
     * @param a the first Node
     * @param b the second Node
     * @return a {@link Set} of {@link Node}s that lie between <code>a</code> and <code>b</code>
     */
    private Set<Node> nodesBetween(Node a, Node b) {
        return NODES_BETWEEN_CACHE.computeIfAbsent(() -> { // Do not compute if already cached

            // Determine bounds of the enclosing rectangle of a and b
            int minY = Math.min(a.y(), b.y());
            int maxY = Math.max(a.y(), b.y());
            int minX = Math.min(a.x(), b.x());
            int maxX = Math.max(a.x(), b.x());

            Set<Node> res = new HashSet<>();
            if (a.x() == b.x()) { // If a and b lie on same column, return nodes of this row
                for (int y = minY + 1; y < maxY; y++)
                    res.add(Node.at(y, minX));
            } else if (a.y() == b.y()) { // If a and b lie on same row, return nodes of this row
                for (int x = minX + 1; x < maxX; x++)
                    res.add(Node.at(minY, x));
            } else {
                // Compute minimal dy and dx steps that are the greatest divisor of the total difference between a and b
                int dy = b.y() - a.y();
                int dx = b.x() - a.x();
                int gcd = Math.abs(MathUtil.gcd(dy, dx));
                dy /= gcd;
                dx /= gcd;

                // Add all nodes that are n steps of size (dy|dx) between a and b
                for (int y = a.y(), x = a.x(); y != b.y() || x != b.x(); y += dy, x += dx)
                    res.add(Node.at(y, x));
            }
            return res;

        }, a, b);
    }

    /**
     * Determines whether a given node is a possible successor of <code>this</code> pattern.
     *
     * @param nextNode the next node to check
     * @return true iff <code>nextNode</code> is a valid successor of <code>this</code>
     */
    private boolean validSuccessor(Node nextNode) {
        if (lastNode == null)
            return true;

        return nodesBetween(lastNode, nextNode)
                .stream()
                .noneMatch(unusedNodes::contains);
    }

    /**
     * Creates a new {@link Pattern} that starts with the {@link Node}s of <code>this</code> pattern and ends with a given node.
     *
     * @param nextNode the {@link Node} to append to <code>this</code>
     * @return the created {@link Pattern}
     */
    private Pattern append(Node nextNode) {
        Set<Node> newUnusedNodes = new HashSet<>(unusedNodes.size());
        newUnusedNodes.addAll(unusedNodes);
        newUnusedNodes.remove(nextNode);
        return new Pattern(nextNode, newUnusedNodes);
    }

    /**
     * Creates a {@link java.awt.Rectangle} representing the bounds of the {@link #unusedNodes} of this {@link Pattern}.
     *
     * @return a new {@link Rectangle} instance
     */
    private Rectangle bounds() {
        int minY = lastNode.y, maxY = lastNode.y;
        int minX = lastNode.x, maxX = lastNode.x;
        for (var node : unusedNodes) {
            if (node.y < minY)
                minY = node.y;
            else if (node.y > maxY)
                maxY = node.y;

            if (node.x < minX)
                minX = node.x;
            else if (node.x > maxX)
                maxX = node.x;
        }
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    /**
     * Helper method to map all {@link Node}s of this {@link Pattern} by some mapper {@link Function}.
     *
     * @param mapper the {@link Function} that maps one {@link Node} to another
     * @return a new {@link Pattern} instance with the mapped nodes
     */
    private Pattern map(Function<Node, Node> mapper) {
        return new Pattern(mapper.apply(lastNode), unusedNodes.stream().map(mapper).collect(Collectors.toSet()));
    }

    /**
     * Shifts all {@link Node}s of this pattern by (dy|dx)
     *
     * @param dy the y-shift
     * @param dx the x-shift
     * @return a new {@link Pattern} instance with shifted {@link Node}s
     */
    private Pattern shift(int dy, int dx) {
        return map(node -> Node.at(node.y + dy, node.x + dx));
    }

    /**
     * Flips the y and x corrdinates for {@link Node}s of this pattern
     *
     * @return a new {@link Pattern} instance with transposed {@link Node}s
     */
    private Pattern transpose() {
        return map(node -> Node.at(node.x, node.y));
    }

    /**
     * Flips all {@link Node}s of this pattern vertically, such that the top row becomes the bottom row (and vice versa)
     *
     * @param height the height of this pattern
     * @return a new {@link Pattern} instance with flipped {@link Node}s
     */
    private Pattern flipVertically(int height) {
        return map(node -> Node.at(height - node.y - 1, node.x));
    }

    /**
     * Flips all {@link Node}s of this pattern horizontally, such that the left column becomes the right column (and vice versa)
     *
     * @param width the width of this pattern
     * @return a new {@link Pattern} instance with flipped {@link Node}s
     */
    private Pattern flipHorizontally(int width) {
        return map(node -> Node.at(node.y, width - node.x - 1));
    }

    /**
     * Removes the x-th column of this pattern.
     *
     * @param x the index of the column to remove
     * @return a new {@link Pattern} instance without the column at index x
     */
    private Pattern dropColumn(int x) {
        return map(node -> node.x < x ? node : Node.at(node.y, node.x - 1));
    }

    /**
     * Removes the y-th row of this pattern.
     *
     * @param y the index of the row to remove
     * @return a new {@link Pattern} instance without the row at index y
     */
    private Pattern dropRow(int y) {
        return map(node -> node.y < y ? node : Node.at(node.y - 1, node.x));
    }

    /**
     * Searches for a column index where no {@link Node} in the set of {@link #unusedNodes} exists.
     *
     * @param width the width of this {@link Pattern}
     * @return the index of an empty column or -1 if there is no
     */
    private int findEmptyColumn(int width) {
        boolean[] occupiedColumn = new boolean[width];
        occupiedColumn[lastNode.x] = true;
        for (Node n : unusedNodes)
            occupiedColumn[n.x] = true;
        for (int x = 0; x < width; x++)
            if (!occupiedColumn[x])
                return x;
        return -1;
    }

    /**
     * Searches for a row index where no {@link Node} in the set of {@link #unusedNodes} exists.
     *
     * @param height the height of this {@link Pattern}
     * @return the index of an empty row or -1 if there is no
     */
    private int findEmptyRow(int height) {
        boolean[] occupiedRow = new boolean[height];
        occupiedRow[lastNode.y] = true;
        for (Node n : unusedNodes)
            occupiedRow[n.y] = true;
        for (int y = 0; y < height; y++)
            if (!occupiedRow[y])
                return y;
        return -1;
    }

    /**
     * Minimizes this {@link Pattern} to an equivalent pattern according to the following rules:
     * <p>
     * 1) The smallest (y|x)-coordinate of a minimal pattern is always (0|0)
     * 2) A minimal pattern is either square or landscape
     * 3) The last node of a pattern is always in the top left corner
     * 4) The last node of a square pattern is always in the upper right triangle
     * 5) A minimal pattern of width (or height) <= 2 has no empty rows (columns).
     *
     * @return a new {@link Pattern} instance with the minimized pattern, or <code>this</code> if already minimal
     */
    private Pattern simplify() {
        var bounds = bounds();

        if (bounds.x > 0 || bounds.y > 0) // 1
            return shift(-bounds.y, -bounds.x).simplify();

        if (bounds.height > bounds.width) // 2
            return transpose().simplify();

        if (lastNode.y > bounds.height / 2) // 3
            return flipVertically(bounds.height).simplify();

        if (lastNode.x > bounds.width / 2) // 3
            return flipHorizontally(bounds.width).simplify();

        if (bounds.height == bounds.width && lastNode.y > lastNode.x) // 4
            return transpose().simplify();

        int empty;
        if (bounds.height <= 2 && (empty = findEmptyColumn(bounds.width)) != -1) // 5
            return dropColumn(empty).simplify();

        if (bounds.width <= 2 && (empty = findEmptyRow(bounds.height)) != -1) // 5
            return dropRow(empty).simplify();

        return this;
    }

    /**
     * Computes all valid patterns that start with the {@link Node} sequence of <code>this</code> {@link Pattern}
     *
     * @return a {@link Stream} of all valid successors
     */
    private Stream<Pattern> validSuccessors() {
        return unusedNodes.stream()
                .filter(this::validSuccessor)
                .map(this::append)
                .map(Pattern::simplify);
    }

    /**
     * Computes the number of valid {@link Pattern}s starting with the {@link Node} sequence of <code>this</code> {@link Pattern}
     *
     * @param length    the current number of used nodes in <code>this</code> pattern
     * @param minLength the minmal number of nodes in a valid pattern, e.g. 4
     * @return the total number of valid patterns starting with <code>this</code>
     */
    private long countValidPatterns(int length, int minLength) {
        return COUNT_VALID_PATTERNS_CACHE.computeIfAbsent(() -> { // Do not compute if already cached

            long res = (length >= minLength ? 1L : 0L)
                    + validSuccessors()
                    .mapToLong(nextPattern -> nextPattern.countValidPatterns(length + 1, minLength))
                    .sum();
            if (res < 0L)
                throw new ArithmeticException("Long overflow"); // Prepare for potentially gigantic numbers :'(
            else return res;

        }, this, length, minLength);
    }

    /**
     * Computes the number of valid {@link Pattern}s, assuming <code>this</code> is the empty pattern.
     *
     * @param minLength the minmal number of nodes in a valid pattern, e.g. 4
     * @return the total number of valid patterns in a (size x size) grid
     */
    public long countValidPatterns(int minLength) {
        return countValidPatterns(0, minLength);
    }

    /**
     * A node in a pattern.
     *
     * @param y the y-coordinate of this node, indexed 0 (top) to height-1 (bottom)
     * @param x the x-coordinate of this node, indexed 0 (left) to width-1 (right)
     */
    private record Node(int y, int x) {
        // A cache to store the node instances in order to reduce memory overhead
        private static final Cache<Node> CACHE = new Cache<>();

        /**
         * Creates a new {@link Node} at the given position or returns a cached instance.
         *
         * @param y the y-coordinate of this node, indexed 0 (top) to height-1 (bottom)
         * @param x the x-coordinate of this node, indexed 0 (left) to width-1 (right)
         * @return a new {@link Node} instance or a cached one
         */
        public static Node at(int y, int x) {
            return CACHE.computeIfAbsent(() -> new Node(y, x), y, x);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return y == node.y && x == node.x;
        }

        @Override
        public int hashCode() {
            return Objects.hash(y, x);
        }

        @Override
        public String toString() {
            return "(" + y + "|" + x + ")";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pattern pattern = (Pattern) o;
        return Objects.equals(lastNode, pattern.lastNode) && unusedNodes.equals(pattern.unusedNodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastNode, unusedNodes);
    }

    /**
     * Creates a {@link String} representation of this {@link Pattern} for debugging purpose.
     *
     * @return the last {@link Node} of this pattern, followed by the set of {@link #unusedNodes}.
     */
    @Override
    public String toString() {
        return lastNode + " -> {" + unusedNodes.stream().map(Node::toString).collect(Collectors.joining(", ")) + "}";
    }
}
