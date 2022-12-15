package de.javaabc.lockpatterns.util;

import java.util.function.Supplier;

public class MathUtil {
    /**
     * Computes the greatest common divisor of two numbers.
     *
     * @param a the first number
     * @param b the second number
     * @return the largest number that divides both a and b.
     */
    public static int gcd(int a, int b) {
        for (int tmp; b != 0; a = tmp) {
            tmp = b;
            b = a % b;
        }
        return a;
    }

    /**
     * Helper method to test if a given array contains a certain number.
     *
     * @param array the array to walk through
     * @param n     the number to find
     * @return true iff <code>n</code> is in <code>array</code>
     */
    public static boolean arrayContains(int[] array, int n) {
        for (int i : array)
            if (i == n)
                return true;
        return false;
    }

    /**
     * Helper method to successively compare different aspects of two elements.
     * <p>
     * Use this method like
     *
     * <pre>
     * int comparisonResult = multiCompare(
     *     () -> a.compareTo(b),
     *     () -> c.compareTo(d),
     *     ...
     *     () -> x.compareTo(y)
     * );
     * </pre>
     * <p>
     * where the top level comparison is the first, and details are compared afterwards.
     *
     * @param criteria multiple {@link Supplier}s that each produce an integer representing the result of one comparison
     * @return an int value representing the result of the comparison
     */
    public static int multiCompare(Supplier<Integer>... criteria) {
        int dif;
        for (var criterion : criteria) {
            if ((dif = criterion.get()) != 0)
                return dif;
        }
        return 0;
    }
}
