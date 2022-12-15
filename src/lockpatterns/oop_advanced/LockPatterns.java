package lockpatterns.oop_advanced;

/**
 * OBJECT ORIENTED APPROACH #2
 *
 * - Can also handle 4x4 patterns (~20s runtime)
 * - Could handle 5x5 etc. in theory, but still too slow (And maybe also memory limitations)
 */
public class LockPatterns {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis(); // Start timer

        long res = new Pattern(4).countValidPatterns(4);

        long stopTime = System.currentTimeMillis(); // Stop timer

        System.out.println(res + " (" + (stopTime - startTime) + " ms)"); // Print result
    }
}
