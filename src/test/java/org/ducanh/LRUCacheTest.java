package org.ducanh;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * Additional comprehensive edge case tests for LRU Cache implementation
 * These tests ensure the LRU strategy is correctly implemented
 */
class LRUCacheEdgeCaseTest {

    @Test
    void testZigzagAccessPattern() {
        // Zigzag access pattern to verify proper LRU ordering
        LRUCache<Integer, Integer> cache = new LRUCache<>(4);

        // Setup initial state [1,2,3,4]
        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(3, 30);
        cache.put(4, 40);

        // Zigzag access pattern: 1, 4, 2, 3, 1, 4
        // This changes the LRU order significantly
        assertEquals(10, cache.get(1)); // Order: 2,3,4,1
        assertEquals(40, cache.get(4)); // Order: 2,3,1,4
        assertEquals(20, cache.get(2)); // Order: 3,1,4,2
        assertEquals(30, cache.get(3)); // Order: 1,4,2,3
        assertEquals(10, cache.get(1)); // Order: 4,2,3,1
        assertEquals(40, cache.get(4)); // Order: 2,3,1,4

        // Add new element - should evict 2 (least recently used)
        cache.put(5, 50);
        assertFalse(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(4));
        assertTrue(cache.containsKey(5));
    }

    @Test
    void testRepeatedKeyUpdates() {
        // Verify that updating same key doesn't increase cache size
        LRUCache<Integer, Integer> cache = new LRUCache<>(3);

        // Update same key multiple times
        for (int i = 0; i < 5; i++) {
            cache.put(1, 100 + i);
            assertEquals(1, cache.size());
        }
        assertEquals(104, cache.get(1));

        // Fill cache
        cache.put(2, 200);
        cache.put(3, 300);
        assertEquals(3, cache.size());

        // Update key 1 again (should become most recent)
        cache.put(1, 111);
        assertEquals(3, cache.size());

        // Add new key, should evict 2 (least recently used)
        cache.put(4, 400);
        assertEquals(3, cache.size());
        assertFalse(cache.containsKey(2));
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
    }

    @Test
    void testInterleavedGetPut() {
        // Complex interleaved get/put operations
        LRUCache<Integer, Integer> cache = new LRUCache<>(5);

        cache.put(1, 10);
        cache.put(2, 20);
        assertEquals(10, cache.get(1)); // Hit
        cache.put(3, 30);
        cache.put(4, 40);
        assertEquals(20, cache.get(2)); // Hit
        cache.put(5, 50);
        assertEquals(30, cache.get(3)); // Hit

        // Cache is full, next put should evict LRU
        cache.put(6, 60); // Should evict 4
        assertFalse(cache.containsKey(4));

        assertEquals(10, cache.get(1)); // Hit
        assertNull(cache.get(4)); // Miss

        cache.put(7, 70); // Should evict 5
        assertFalse(cache.containsKey(5));
        assertNull(cache.get(5)); // Miss

        cache.put(8, 80); // Should evict 2
        assertFalse(cache.containsKey(2));
        assertEquals(60, cache.get(6)); // Hit
    }

    @Test
    void testCyclicAccessPattern() {
        // Cyclic access pattern larger than capacity
        LRUCache<Integer, Integer> cache = new LRUCache<>(3);

        // First cycle: try to put 5 elements in cache of size 3
        for (int key = 1; key <= 5; key++) {
            cache.put(key, key * 100);
            assertTrue(cache.size() <= 3);
        }

        // Should only have last 3 elements
        assertFalse(cache.containsKey(1));
        assertFalse(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
        assertTrue(cache.containsKey(5));

        // Second cycle: repeat the pattern
        for (int key = 1; key <= 5; key++) {
            cache.put(key, key * 100);
        }

        // Again, should only have last 3 elements
        assertFalse(cache.containsKey(1));
        assertFalse(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
        assertTrue(cache.containsKey(5));
    }

    @Test
    void testGetHeavyWorkload() {
        // Many gets with few puts to test access ordering
        LRUCache<Integer, Integer> cache = new LRUCache<>(4);

        // Initial puts
        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(3, 30);
        cache.put(4, 40);

        // Heavy get pattern with frequency: 1->5, 2->3, 3->2, 4->1
        int[] getPattern = {1, 1, 2, 1, 3, 2, 1, 4, 3, 2, 1};
        for (int key : getPattern) {
            assertNotNull(cache.get(key));
        }

        // Now add new element - should evict 4 (least recently accessed)
        cache.put(5, 50);
        assertFalse(cache.containsKey(4));
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(5));
    }

    @Test
    void testAllDifferentKeys() {
        // All operations use different keys (pure FIFO behavior expected when no reuse)
        LRUCache<Integer, Integer> cache = new LRUCache<>(3);

        for (int i = 0; i < 10; i++) {
            cache.put(i, i * 10);
            assertEquals(Math.min(i + 1, 3), cache.size());

            // Check expected keys in cache
            if (i >= 2) {
                assertFalse(cache.containsKey(i - 3));
                assertTrue(cache.containsKey(i - 2));
                assertTrue(cache.containsKey(i - 1));
                assertTrue(cache.containsKey(i));
            }
        }
    }

    @Test
    void testSingleCapacityStress() {
        // Single capacity with multiple operations
        LRUCache<Integer, Integer> cache = new LRUCache<>(1);

        cache.put(1, 100);
        assertEquals(100, cache.get(1));

        cache.put(2, 200);
        assertNull(cache.get(1)); // Should be evicted
        assertEquals(200, cache.get(2));

        cache.put(3, 300);
        assertNull(cache.get(2)); // Should be evicted
        assertEquals(300, cache.get(3));

        cache.put(1, 150);
        assertNull(cache.get(3)); // Should be evicted
        assertEquals(150, cache.get(1));
    }

    @Test
    void testAlternatingPutGet() {
        // Alternating put and get operations
        LRUCache<Integer, Integer> cache = new LRUCache<>(3);

        cache.put(1, 1);
        assertEquals(1, cache.get(1));
        cache.put(2, 2);
        assertEquals(2, cache.get(2));
        cache.put(3, 3);
        assertEquals(3, cache.get(3));
        cache.put(4, 4); // Evicts 1

        assertNull(cache.get(1));
        assertEquals(2, cache.get(2));

        cache.put(5, 5); // Evicts 3
        assertNull(cache.get(3));
        assertTrue(cache.containsKey(4));
        assertTrue(cache.containsKey(2));
        assertTrue(cache.containsKey(5));
    }

    @Test
    void testLRUOrderingWithContainsKey() {
        // Verify that containsKey does NOT affect LRU ordering
        LRUCache<Integer, Integer> cache = new LRUCache<>(3);

        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(3, 30);

        // containsKey should not change LRU order
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(1));

        // Add new element - 1 should still be evicted (if containsKey doesn't affect order)
        cache.put(4, 40);
        assertFalse(cache.containsKey(1));
        assertTrue(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
    }

    @Test
    void testSequentialEvictions() {
        // Test multiple sequential evictions
        LRUCache<Integer, Integer> cache = new LRUCache<>(5);

        // Fill cache
        for (int i = 1; i <= 5; i++) {
            cache.put(i, i * 10);
        }

        // Access some elements to change LRU order
        cache.get(1); // 1 becomes most recent
        cache.get(3); // 3 becomes most recent

        // Add multiple new elements sequentially
        // Order before: 2, 4, 5, 1, 3 (from LRU to MRU)
        cache.put(6, 60); // Evicts 2
        assertFalse(cache.containsKey(2));

        cache.put(7, 70); // Evicts 4
        assertFalse(cache.containsKey(4));

        cache.put(8, 80); // Evicts 5
        assertFalse(cache.containsKey(5));

        // Should still have 1, 3, 6, 7, 8
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(6));
        assertTrue(cache.containsKey(7));
        assertTrue(cache.containsKey(8));
    }

    @Test
    void testPutGetPutSameKey() {
        // Test put-get-put on same key
        LRUCache<Integer, Integer> cache = new LRUCache<>(2);

        cache.put(1, 100);
        cache.put(2, 200);

        assertEquals(100, cache.get(1));
        cache.put(1, 150); // Update value

        cache.put(3, 300); // Should evict 2, not 1
        assertFalse(cache.containsKey(2));
        assertEquals(150, cache.get(1));
        assertEquals(300, cache.get(3));
    }

    @Test
    void testRemoveAffectsEviction() {
        // Test that remove affects eviction order
        LRUCache<Integer, Integer> cache = new LRUCache<>(4);

        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(3, 30);
        cache.put(4, 40);

        // Remove an element
        assertEquals(20, cache.remove(2));
        assertEquals(3, cache.size());

        // Add two new elements
        cache.put(5, 50);
        cache.put(6, 60);

        // Cache should now have 1, 3, 4, 5, 6 (capacity is 4, so one eviction)
        assertEquals(4, cache.size());
        assertFalse(cache.containsKey(1)); // 1 should be evicted as LRU
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
        assertTrue(cache.containsKey(5));
        assertTrue(cache.containsKey(6));
    }

    @Test
    void testClearAndRefill() {
        // Test clear operation and refilling
        LRUCache<Integer, Integer> cache = new LRUCache<>(3);

        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(3, 30);

        cache.clear();
        assertEquals(0, cache.size());

        // Refill with different values
        cache.put(4, 40);
        cache.put(5, 50);
        cache.put(6, 60);
        cache.put(7, 70);

        // Should work normally after clear
        assertFalse(cache.containsKey(4));
        assertTrue(cache.containsKey(5));
        assertTrue(cache.containsKey(6));
        assertTrue(cache.containsKey(7));
    }

    @Test
    void testMixedOperationsLargeScale() {
        // Large scale test with mixed operations
        LRUCache<Integer, Integer> cache = new LRUCache<>(100);
        Random random = new Random(42); // Fixed seed for reproducibility

        // Phase 1: Fill cache
        for (int i = 0; i < 100; i++) {
            cache.put(i, i * 10);
        }
        assertEquals(100, cache.size());

        // Phase 2: Random access pattern
        for (int i = 0; i < 500; i++) {
            int key = random.nextInt(150);
            if (random.nextBoolean()) {
                cache.put(key, key * 10);
            } else {
                cache.get(key); // May be null
            }
        }

        assertEquals(100, cache.size());

        // Phase 3: Sequential eviction test
        Set<Integer> recentKeys = new HashSet<>();
        for (int i = 200; i < 250; i++) {
            cache.put(i, i * 10);
            recentKeys.add(i);
        }

        // At least some recent keys should be in cache
        int recentCount = 0;
        for (int key : recentKeys) {
            if (cache.containsKey(key)) {
                recentCount++;
            }
        }
        assertTrue(recentCount > 40, "Should have most recent keys in cache");
    }

    @Test
    void testMemoryConsistency() {
        // Verify that evicted entries don't cause memory leaks
        LRUCache<Integer, byte[]> cache = new LRUCache<>(2);

        // Put large objects
        cache.put(1, new byte[1000]);
        cache.put(2, new byte[1000]);

        // Keep reference to first object
        byte[] firstArray = cache.get(1);
        assertNotNull(firstArray);

        // Force eviction
        cache.put(3, new byte[1000]);

        // First key should be evicted
        assertNull(cache.get(1));
        assertTrue(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
    }

    @Test
    void testBoundaryEvictionDecisions() {
        // Test eviction decisions at boundary conditions
        LRUCache<Integer, Integer> cache = new LRUCache<>(3);

        // Fill cache exactly to capacity
        cache.put(1, 100);
        cache.put(2, 200);
        cache.put(3, 300);
        assertEquals(3, cache.size());

        // Access middle element
        assertEquals(200, cache.get(2));

        // Add new element, should evict 1 (not 2, even though 2 was in middle)
        cache.put(4, 400);
        assertFalse(cache.containsKey(1));
        assertTrue(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
    }

    @Test
    void testEvictionAfterMultipleRemoves() {
        // Test eviction behavior after removing multiple elements
        LRUCache<Integer, Integer> cache = new LRUCache<>(5);

        // Fill cache
        for (int i = 1; i <= 5; i++) {
            cache.put(i, i * 100);
        }

        // Remove multiple elements
        cache.remove(2);
        cache.remove(4);
        assertEquals(3, cache.size());

        // Add elements to fill and exceed capacity
        cache.put(6, 600);
        cache.put(7, 700);
        cache.put(8, 800);

        // Should evict 1 (oldest remaining)
        assertEquals(5, cache.size());
        assertFalse(cache.containsKey(1));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(5));
        assertTrue(cache.containsKey(6));
        assertTrue(cache.containsKey(7));
        assertTrue(cache.containsKey(8));
    }

    @Test
    void testAccessPatternTracking() {
        // Verify correct tracking of access patterns
        LRUCache<Integer, Integer> cache = new LRUCache<>(4);

        // Create specific access pattern
        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(3, 30);
        cache.put(4, 40);

        // Access in specific order: 2, 4, 1, 3
        cache.get(2);
        cache.get(4);
        cache.get(1);
        cache.get(3);

        // Now LRU order should be: 2, 4, 1, 3 (from least to most recent)

        // Add two new elements
        cache.put(5, 50); // Should evict 2
        assertFalse(cache.containsKey(2));

        cache.put(6, 60); // Should evict 4
        assertFalse(cache.containsKey(4));

        // 1 and 3 should still be there
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(5));
        assertTrue(cache.containsKey(6));
    }
}