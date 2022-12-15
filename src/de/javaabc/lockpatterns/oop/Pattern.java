package de.javaabc.lockpatterns.oop;

import de.javaabc.lockpatterns.util.Cache;
import de.javaabc.lockpatterns.util.MathUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class Pattern {
    // A Cache for the result of the nodesBetween() function
    private static final Cache<List<Node>> NODES_BETWEEN_CACHE = new Cache<>();

    // The width and height of the pattern, e.g. 3x3
    private final int size;

    // The reference to the previous pattern, e.g. (0|0)-(0|1)-(1|0) for the pattern (0|0)-(0|1)-(1|0)-(1|1)
    private final Pattern previous;

    // The last Node of this pattern, e.g. (1|1) for the pattern (0|0)-(0|1)-(1|0)-(1|1)
    private final Node lastNode;

    // The number of nodes in this pattern
    private final int nodeCount;

    // The set of all nodes in a (size x size) pattern that have not been used in this pattern so far
    private final SortedSet<Node> unusedNodes;

    /**
     * Creates a new pattern.
     *
     * @param size        the width and height of the grid
     * @param previous    the reference to the previous pattern, e.g. (0|0)-(0|1)-(1|0) for the pattern (0|0)-(0|1)-(1|0)-(1|1)
     * @param lastNode    the last {@link Node} of this pattern, e.g. (1|1) for the pattern (0|0)-(0|1)-(1|0)-(1|1)
     * @param nodeCount   the number of nodes in this pattern
     * @param unusedNodes the set of all nodes in a (size x size) pattern that have not been used in this pattern so far
     */
    public Pattern(int size, Pattern previous, Node lastNode, int nodeCount, SortedSet<Node> unusedNodes) {
        this.size = size;
        this.previous = previous;
        this.lastNode = lastNode;
        this.nodeCount = nodeCount;
        this.unusedNodes = unusedNodes;
    }

    /**
     * Returns the set of all nodes in a (size x size) pattern
     *
     * @param size the width and height of the patterns
     * @return a {@link SortedSet} of all possible {@link Node}s
     */
    private static SortedSet<Node> allNodes(int size) {
        SortedSet<Node> res = new TreeSet<>(Node::compareTo);
        for (int y = 0; y < size; y++)
            for (int x = 0; x < size; x++)
                res.add(new Node(y, x));
        return res;
    }

    /**
     * Constructor for the empty pattern (which is invalid but needed to compute its successors)
     *
     * @param size the width and height of the grid
     */
    public Pattern(int size) {
        this(size, null, null, 0, allNodes(size));
    }

    /**
     * Computes all {@link Node}s that lie exactly on a straight line that connects two given nodes.
     *
     * @param a the first Node
     * @param b the second Node
     * @return a {@link List} of {@link Node}s that lie between <code>a</code> and <code>b</code>
     */
    private static List<Node> nodesBetween(Node a, Node b) {
        return NODES_BETWEEN_CACHE.computeIfAbsent(() -> { // Do not compute if already cached

            // Determine bounds of the enclosing rectangle of a and b
            int minY = Math.min(a.y(), b.y());
            int maxY = Math.max(a.y(), b.y());
            int minX = Math.min(a.x(), b.x());
            int maxX = Math.max(a.x(), b.x());

            List<Node> res = new ArrayList<>();
            if (a.x() == b.x()) { // If a and b lie on same column, return nodes of this row
                for (int y = minY + 1; y < maxY; y++)
                    res.add(new Node(y, minX));
            } else if (a.y() == b.y()) { // If a and b lie on same row, return nodes of this row
                for (int x = minX + 1; x < maxX; x++)
                    res.add(new Node(minY, x));
            } else {
                // Compute minimal dy and dx steps that are the greatest divisor of the total difference between a and b
                int dy = b.y() - a.y();
                int dx = b.x() - a.x();
                int gcd = Math.abs(MathUtil.gcd(dy, dx));
                dy /= gcd;
                dx /= gcd;

                // Add all nodes that are n steps of size (dy|dx) between a and b
                for (int y = a.y(), x = a.x(); y != b.y() || x != b.x(); y += dy, x += dx)
                    res.add(new Node(y, x));
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
    private boolean illegalNextNode(Node nextNode) {
        if (lastNode != null)
            for (Node intermediateNode : nodesBetween(lastNode, nextNode))
                if (unusedNodes.contains(intermediateNode))
                    return true;
        return false;
    }

    /**
     * Recursively counts the number of valid patterns that start with the {@link Node}s of <code>this</code> pattern.
     *
     * @param minLength the minimal length for a valid pattern, e.g. 4
     * @return The total number of valid patterns starting with <code>this</code>
     */
    public int countValidSuccessors(int minLength) {
        int count = nodeCount >= minLength ? 1 : 0;
        for (Node nextNode : unusedNodes)
            if (!illegalNextNode(nextNode)) {
                SortedSet<Node> nextUnused = new TreeSet<>(unusedNodes);
                nextUnused.remove(nextNode);
                Pattern next = new Pattern(size, this, nextNode, nodeCount + 1, nextUnused);
                count += next.countValidSuccessors(minLength);
            }
        return count;
    }

    /**
     * A node in a pattern.
     *
     * @param y the y-coordinate, indexed 0 (top) to size-1 (bottom)
     * @param x the x-coordinate, indexed 0 (left) to size-1 (right)
     */
    public record Node(int y, int x) implements Comparable<Node> {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return y == node.y && x == node.x;
        }

        @Override
        public String toString() {
            return "(" + y + "|" + x + ")";
        }

        @Override
        public int compareTo(Node n) {
            return MathUtil.multiCompare(
                    () -> Integer.compare(y, n.y),
                    () -> Integer.compare(x, n.x)
            );
        }
    }


    /**
     * @return a {@link String} representation like (0|1)-(1|0)-(1|1)
     */
    @Override
    public String toString() {
        if (nodeCount == 0)
            return "";

        if (nodeCount == 1)
            return lastNode.toString();

        return previous + "-" + lastNode;
    }
}
