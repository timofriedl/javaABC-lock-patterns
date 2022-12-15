package de.javaabc.lockpatterns.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A cache to store computed values, indexed by one or multiple key {@link Object}s.
 *
 * @param <Value> the type of the values to store
 */
public final class Cache<Value> {
    // The map that contains all values
    private final Map<MultiKey, Value> table;

    /**
     * Creates a new cache instance.
     */
    public Cache() {
        table = new HashMap<>();
    }

    /**
     * If the {@link Value} behind the given <code>keys</code> is present, return the value.
     * Otherwise, execute the <code>computation</code> {@link Supplier} to receive the value first.
     *
     * @param computation a {@link Supplier} that computes the {@link Value} for the given <code>keys</code>
     * @param keys        one or multiple {@link Object}s that together act as key elements
     * @return the cached or computed value
     */
    public Value computeIfAbsent(Supplier<Value> computation, Object... keys) {
        var cachedValue = table.get(new MultiKey(keys));
        if (cachedValue == null) {
            cachedValue = computation.get();
            table.put(new MultiKey(keys), computation.get());
            if (table.size() % 10000 == 0)
                System.out.print(".");
        }
        return cachedValue;
    }

    /**
     * Helper record to use more than one {@link Object} as key.
     *
     * @param keys The key objects
     */
    private record MultiKey(Object[] keys) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MultiKey multiKey = (MultiKey) o;
            return Arrays.equals(keys, multiKey.keys);
        }

        @Override
        public int hashCode() {
            return Arrays.deepHashCode(keys);
        }
    }

    @Override
    public String toString() {
        return "CACHE[" + table.size() + "] {\n    " + table.entrySet().stream()
                .map(e -> e.getKey() + " ==> " + e.getValue())
                .collect(Collectors.joining("\n    ")) + "\n}";
    }
}
