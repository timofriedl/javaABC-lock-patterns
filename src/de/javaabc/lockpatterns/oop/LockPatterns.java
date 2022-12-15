package de.javaabc.lockpatterns.oop;

/**
 * OBJECT ORIENTED APPROACH
 *
 * - Could handle 4x4 patterns in theory, but way too slow
 */
public class LockPatterns {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis(); // Start timer

        int res = new Pattern(3).countValidSuccessors(4);

        long stopTime = System.currentTimeMillis(); // Stop timer

        System.out.println(res + " (" + (stopTime - startTime) + " ms)"); // Print result
    }
}
