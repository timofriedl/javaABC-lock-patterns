package de.javaabc.lockpatterns._bruteforce;

/**
 * BRUTEFORCE
 *
 * - Only works for 3x3 patterns
 * - Not the fastest, but the simplest approach
 */
public class LockPatterns {
    /**
     * Checks whether a given node is an invalid successor of a given last node, given what nodes have already been visited.
     *
     * @param last    the node to connect from
     * @param node    the node to connect to
     * @param visited the array that maps the node id to a boolean stating if this node has already been visited
     * @return true iff the given next node is NOT a valid successor
     */
    private static boolean illegalNextNode(int last, int node, boolean[] visited) {
        return last % 3 == 1 && node == last + 2 && !visited[node - 1] // Rows left to right
                || last % 3 == 0 && node == last - 2 && !visited[node + 1] // Rows right to left
                || (last + 2) / 3 == 1 && node == last + 6 && !visited[node - 3] // Columns top to bottom
                || (last + 2) / 3 == 3 && node == last - 6 && !visited[node + 3] // Columns bottom to top
                || (!visited[5] && (last == 1 && node == 9 || last == 9 && node == 1 || last == 3 && node == 7 || last == 7 && node == 3)); // Diagonal
    }

    /**
     * Determines whether a given pattern is valid or not.
     *
     * @param pattern an <code>int</code> that represents the reverse (!) order of nodes to connect
     * @return true iff the given pattern is valid
     */
    private static boolean validPattern(int pattern) {
        boolean[] visited = new boolean[10]; // Has a specific node already been visited?
        for (int last = 0; pattern > 0; pattern /= 10) { // Iterate through digits from right to left
            int node = pattern % 10; // Last digit
            if (node == 0 // End of pattern reached
                    || visited[node] // Node already visited
                    || last != 0 && illegalNextNode(last, node, visited)) // Illegal transition
                return false;

            visited[node] = true;
            last = node;
        }
        return true; // No illegal transition -> Pattern is valid
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis(); // Start timer

        int count = 0;
        for (int pattern = 1234; pattern <= 987654321; pattern++) // Go through all possible patterns
            if (validPattern(pattern)) // Count if valid
                count++;

        long stopTime = System.currentTimeMillis(); // Stop timer

        System.out.println(count + " (" + (stopTime - startTime) + " ms)"); // Print result
    }
}
