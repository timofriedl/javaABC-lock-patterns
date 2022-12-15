package lockpatterns._treesearch;

import java.util.Arrays;

import static lockpatterns.util.MathUtil.arrayContains;

/**
 * TREESEARCH
 *
 * - Can only handle 3x3 patterns
 * - Faster than bruteforce
 */
public class LockPatterns {
    /**
     * Checks whether a given node is an invalid successor of a given last node, given the path of already visited nodes.
     *
     * @param path the array of visited nodes
     * @param node the next node to connect to
     * @return true iff the given next node is NOT a valid successor
     */
    private static boolean illegalNextNode(int[] path, int node) {
        int last = path[path.length - 1];
        return last % 3 == 1 && node == last + 2 && !arrayContains(path, node - 1) // Rows left to right
                || last % 3 == 0 && node == last - 2 && !arrayContains(path, node + 1) // Rows right to left
                || (last + 2) / 3 == 1 && node == last + 6 && !arrayContains(path, node - 3) // Columns top to bottom<
                || (last + 2) / 3 == 3 && node == last - 6 && !arrayContains(path, node + 3) // Columns bottom to top
                || (!arrayContains(path, 5) && (last == 1 && node == 9 || last == 9 && node == 1 || last == 3 && node == 7 || last == 7 && node == 3)); // diagonal
    }

    /**
     * Counts the number of valid successor patterns, given the path of a start pattern.
     *
     * @param path      the current sequence of visited nodes
     * @param minLength the minimal length for a valid pattern, e.g. 4
     * @return the number of valid patterns starting with the sequence defined in <code>path</code>
     */
    public static int countValidSuccessors(int[] path, int minLength) {
        int count = path.length >= minLength ? 1 : 0;

        for (int node = 1; node <= 9; node++)
            if (!arrayContains(path, node) && (path.length == 0 || !illegalNextNode(path, node))) {
                int[] copy = Arrays.copyOf(path, path.length + 1);
                copy[path.length] = node;
                count += countValidSuccessors(copy, minLength);
            }

        return count;
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis(); // Start timer

        int res = countValidSuccessors(new int[]{}, 4);

        long stopTime = System.currentTimeMillis(); // Stop timer

        System.out.println(res + " (" + (stopTime - startTime) + " ms)"); // Print result
    }
}
