package org.ducanh;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * Comprehensive edge case tests for LFU Cache implementation
 * These tests ensure the LFU (Least Frequently Used) strategy is correctly implemented
 */
public class LFUCacheEdgeCaseTest {

    @Test
    void testBasicFrequencyEviction() {
        // Test basic frequency-based eviction
        LFUCache<Integer, Integer> cache = new LFUCache<>(3);

        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(3, 30);

        // Access key 1 and 2 multiple times to increase frequency
        assertEquals(10, cache.get(1)); // freq(1) = 2
        assertEquals(10, cache.get(1)); // freq(1) = 3
        assertEquals(20, cache.get(2)); // freq(2) = 2
        // freq(3) = 1 (only put, no get)

        // Add new element - should evict 3 (least frequently used with freq=1)
        cache.put(4, 40);
        assertFalse(cache.containsKey(3));
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(2));
        assertTrue(cache.containsKey(4));
    }

    @Test
    void testTieBreakingWithRecency() {
        // When frequencies are equal, evict least recently used
        LFUCache<Integer, Integer> cache = new LFUCache<>(3);

        cache.put(1, 10);  // freq(1) = 1, time = 1
        cache.put(2, 20);  // freq(2) = 1, time = 2
        cache.put(3, 30);  // freq(3) = 1, time = 3

        // All have same frequency, so should evict 1 (least recent among freq=1)
        cache.put(4, 40);
        assertFalse(cache.containsKey(1));
        assertTrue(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
    }

    @Test
    void testFrequencyIncrementOnGet() {
        // Verify get operations increase frequency
        LFUCache<Integer, Integer> cache = new LFUCache<>(4);

        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(3, 30);
        cache.put(4, 40);

        // Increase frequencies differently
        cache.get(1); // freq(1) = 2
        cache.get(1); // freq(1) = 3
        cache.get(1); // freq(1) = 4

        cache.get(2); // freq(2) = 2
        cache.get(2); // freq(2) = 3

        cache.get(3); // freq(3) = 2
        // freq(4) = 1

        // Add new element - should evict 4 (freq=1)
        cache.put(5, 50);
        assertFalse(cache.containsKey(4));
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(5));
    }

    @Test
    void testPutOnExistingKeyUpdatesFrequency() {
        // Verify that put on existing key increases frequency
        LFUCache<Integer, Integer> cache = new LFUCache<>(3);

        cache.put(1, 100);  // freq(1) = 1
        cache.put(2, 200);  // freq(2) = 1
        cache.put(3, 300);  // freq(3) = 1

        // Update key 1 multiple times
        cache.put(1, 110);  // freq(1) = 2
        cache.put(1, 120);  // freq(1) = 3
        cache.put(1, 130);  // freq(1) = 4

        // Update key 2 once
        cache.put(2, 210);  // freq(2) = 2

        // Now freq(1)=4, freq(2)=2, freq(3)=1
        // Adding new key should evict 3
        cache.put(4, 400);
        assertFalse(cache.containsKey(3));
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(2));
        assertTrue(cache.containsKey(4));
    }

    @Test
    void testComplexFrequencyPattern() {
        // Complex access pattern to test frequency tracking
        LFUCache<Integer, Integer> cache = new LFUCache<>(5);

        // Initial puts
        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(3, 30);
        cache.put(4, 40);
        cache.put(5, 50);

        // Create different frequency levels
        cache.get(1); cache.get(1); cache.get(1); // freq(1) = 4
        cache.get(2); cache.get(2);                // freq(2) = 3
        cache.get(3); cache.get(3);                // freq(3) = 3
        cache.get(4);                               // freq(4) = 2
        // freq(5) = 1

        // Add new element - should evict 5 (lowest frequency)
        cache.put(6, 60);
        assertFalse(cache.containsKey(5));

        // Now we have: 1(freq=4), 2(freq=3), 3(freq=3), 4(freq=2), 6(freq=1)
        // Add another - should evict 6 (lowest frequency=1)
        cache.put(7, 70);
        assertFalse(cache.containsKey(6));

        // Remaining: 1(freq=4), 2(freq=3), 3(freq=3), 4(freq=2), 7(freq=1)
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
        assertTrue(cache.containsKey(7));
    }

    @Test
    void testSingleCapacityWithFrequency() {
        // Single capacity - new element always evicts the only element
        LFUCache<Integer, Integer> cache = new LFUCache<>(1);

        cache.put(1, 100);
        assertEquals(100, cache.get(1)); // freq(1) = 2
        assertEquals(100, cache.get(1)); // freq(1) = 3

        // Even with high frequency, capacity=1 means eviction
        cache.put(2, 200);
        assertNull(cache.get(1));
        assertEquals(200, cache.get(2));

        cache.put(3, 300);
        assertNull(cache.get(2));
        assertEquals(300, cache.get(3));
    }

    @Test
    void testAllSameFrequencyFIFOBehavior() {
        // When all have same frequency, should behave like FIFO
        LFUCache<Integer, Integer> cache = new LFUCache<>(3);

        // Put without any gets - all have frequency 1
        for (int i = 0; i < 10; i++) {
            cache.put(i, i * 10);
            assertEquals(Math.min(i + 1, 3), cache.size());

            // With same frequency, earliest entries are evicted first
            if (i >= 3) {
                // Should have last 3 elements
                assertFalse(cache.containsKey(i - 3));
                assertTrue(cache.containsKey(i - 2));
                assertTrue(cache.containsKey(i - 1));
                assertTrue(cache.containsKey(i));
            }
        }
    }

    @Test
    void testContainsKeyDoesNotAffectFrequency() {
        // Verify containsKey doesn't change frequency
        LFUCache<Integer, Integer> cache = new LFUCache<>(3);

        cache.put(1, 10);  // freq(1) = 1
        cache.put(2, 20);  // freq(2) = 1
        cache.put(3, 30);  // freq(3) = 1

        // containsKey shouldn't affect frequency
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(1));

        // All still have frequency 1, so evict based on insertion order
        cache.put(4, 40);
        assertFalse(cache.containsKey(1)); // First inserted with freq=1
        assertTrue(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
    }

    @Test
    void testGetHeavyWorkloadFrequencyBased() {
        // Many gets to test frequency-based eviction
        LFUCache<Integer, Integer> cache = new LFUCache<>(4);

        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(3, 30);
        cache.put(4, 40);

        // Create frequency pattern: 1->5 times, 2->3 times, 3->2 times, 4->1 time
        int[] getPattern = {1, 1, 2, 1, 3, 2, 1, 4, 3, 2, 1};
        for (int key : getPattern) {
            assertNotNull(cache.get(key));
        }
        // Frequencies: 1->6, 2->4, 3->3, 4->2

        // Add new element - should evict 4 (lowest frequency = 2)
        cache.put(5, 50);
        assertFalse(cache.containsKey(4));
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(5));
    }

    @Test
    void testRemoveResetsFrequency() {
        // Test that removing and re-adding resets frequency
        LFUCache<Integer, Integer> cache = new LFUCache<>(3);

        cache.put(1, 10);
        cache.get(1); cache.get(1); // freq(1) = 3

        cache.put(2, 20);
        cache.get(2); // freq(2) = 2

        cache.put(3, 30); // freq(3) = 1

        // Re-add key 1 - frequency should reset to 1
        cache.put(1, 15);

        // Now freq(1)=1, freq(2)=2, freq(3)=1
        // Add new key, should evict based on frequency=1 and recency
        cache.put(4, 40);

        // Should evict 3 (same freq as 1, but less recent)
        assertFalse(cache.containsKey(3));
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(2));
        assertTrue(cache.containsKey(4));
    }

    @Test
    void testClearResetsAllFrequencies() {
        // Test clear operation resets everything
        LFUCache<Integer, Integer> cache = new LFUCache<>(3);

        cache.put(1, 10);
        cache.get(1); cache.get(1); // freq(1) = 3
        cache.put(2, 20);
        cache.get(2); // freq(2) = 2
        cache.put(3, 30); // freq(3) = 1

        cache.clear();
        assertEquals(0, cache.size());

        // After clear, all new entries start with frequency 1
        cache.put(1, 100);
        cache.put(2, 200);
        cache.put(3, 300);
        cache.put(4, 400);

        // Should evict 1 (first inserted with freq=1)
        assertFalse(cache.containsKey(1));
        assertTrue(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
    }

    @Test
    void testSequentialEvictionsByFrequency() {
        // Test multiple evictions based on frequency
        LFUCache<Integer, Integer> cache = new LFUCache<>(5);

        // Setup with different frequencies
        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(3, 30);
        cache.put(4, 40);
        cache.put(5, 50);

        // Create frequency tiers
        for (int i = 0; i < 4; i++) cache.get(1); // freq(1) = 5
        for (int i = 0; i < 3; i++) cache.get(2); // freq(2) = 4
        for (int i = 0; i < 2; i++) cache.get(3); // freq(3) = 3
        cache.get(4);                              // freq(4) = 2
        // freq(5) = 1

        // Sequential additions should evict in order of frequency
        cache.put(6, 60);  // freq(6) = 1
        assertFalse(cache.containsKey(5)); // freq=1 evicted (oldest among freq=1)

        // Now: 1(f=5), 2(f=4), 3(f=3), 4(f=2), 6(f=1)
        cache.put(7, 70);  // freq(7) = 1
        assertFalse(cache.containsKey(6)); // freq=1 evicted (oldest among freq=1)

        // Now: 1(f=5), 2(f=4), 3(f=3), 4(f=2), 7(f=1)
        cache.put(8, 80);  // freq(8) = 1
        assertFalse(cache.containsKey(7)); // freq=1 evicted (oldest among freq=1)

        // Should have: 1, 2, 3, 4, 8
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
        assertTrue(cache.containsKey(8));
    }

    @Test
    void testMixedOperationsLargeScale() {
        // Large scale test with frequency-based eviction
        LFUCache<Integer, Integer> cache = new LFUCache<>(100);
        Random random = new Random(42);

        // Phase 1: Fill cache
        for (int i = 0; i < 100; i++) {
            cache.put(i, i * 10);
        }

        // Phase 2: Create frequency distribution
        // Access some keys more frequently
        for (int i = 0; i < 500; i++) {
            int key = random.nextInt(100);
            if (key < 20) {
                // Keys 0-19 get accessed more
                cache.get(key);
                cache.get(key);
            } else if (key < 50) {
                // Keys 20-49 get accessed once
                cache.get(key);
            }
            // Keys 50-99 rarely accessed
        }

        // Phase 3: Add new elements
        for (int i = 100; i < 150; i++) {
            cache.put(i, i * 10);
        }

        // Keys 0-19 should mostly remain (high frequency)
        int highFreqCount = 0;
        for (int i = 0; i < 20; i++) {
            if (cache.containsKey(i)) highFreqCount++;
        }
        assertTrue(highFreqCount > 15, "Most high-frequency keys should remain");

        // Keys 50-99 should mostly be evicted (low frequency)
        int lowFreqCount = 0;
        for (int i = 50; i < 100; i++) {
            if (cache.containsKey(i)) lowFreqCount++;
        }
        assertTrue(lowFreqCount < 20, "Most low-frequency keys should be evicted");
    }

    @Test
    void testPutGetPutSameKeyFrequency() {
        // Test put-get-put increases frequency correctly
        LFUCache<Integer, Integer> cache = new LFUCache<>(2);

        cache.put(1, 100);  // freq(1) = 1
        cache.put(2, 200);  // freq(2) = 1

        assertEquals(100, cache.get(1));  // freq(1) = 2
        cache.put(1, 150);  // freq(1) = 3

        // Add new key - should evict 2 (freq=1) not 1 (freq=3)
        cache.put(3, 300);
        assertFalse(cache.containsKey(2));
        assertEquals(150, cache.get(1));
        assertEquals(300, cache.get(3));
    }

    @Test
    void testEvictionAfterMultipleRemoves() {
        // Test eviction after removing elements
        LFUCache<Integer, Integer> cache = new LFUCache<>(5);

        // Setup with frequencies
        for (int i = 1; i <= 5; i++) {
            cache.put(i, i * 100);
        }

        // Create different frequencies
        cache.get(1); cache.get(1); // freq(1) = 3
        cache.get(3);                // freq(3) = 2
        // Others have freq = 1

        // Remove high frequency element
        cache.remove(1);
        cache.remove(3);
        assertEquals(3, cache.size());

        // Add new elements
        cache.put(6, 600);
        cache.put(7, 700);
        cache.put(8, 800);

        // Should evict based on frequency (2, 4, 5 all have freq=1)
        assertEquals(5, cache.size());
        assertFalse(cache.containsKey(2)); // First one with freq=1
    }

    @Test
    void testFrequencyPromotionPattern() {
        // Test that elements can be promoted through frequency tiers
        LFUCache<Integer, Integer> cache = new LFUCache<>(4);

        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(3, 30);
        cache.put(4, 40);

        // Initially all have freq=1
        // Promote key 4 to highest frequency
        for (int i = 0; i < 5; i++) {
            cache.get(4);  // freq(4) increases to 6
        }

        // Promote key 3 to medium frequency
        cache.get(3);
        cache.get(3);  // freq(3) = 3

        // Frequencies: 4->6, 3->3, 2->1, 1->1

        // Add new element - should evict one with freq=1 (1 or 2)
        cache.put(5, 50);
        assertTrue(
                (!cache.containsKey(1) && cache.containsKey(2)) ||
                        (!cache.containsKey(2) && cache.containsKey(1))
        );

        // Add another - should evict the other one with freq=1
        cache.put(6, 60);
        assertFalse(cache.containsKey(1));
        assertFalse(cache.containsKey(2));

        // High frequency keys remain
        assertTrue(cache.containsKey(4));
        assertTrue(cache.containsKey(3));
    }

    @Test
    void testMemoryConsistencyWithFrequency() {
        // Verify proper cleanup with frequency tracking
        LFUCache<Integer, byte[]> cache = new LFUCache<>(2);

        cache.put(1, new byte[1000]);
        cache.put(2, new byte[1000]);

        // Increase frequency of key 2
        byte[] array2 = cache.get(2);
        assertNotNull(array2);
        cache.get(2);  // freq(2) = 3

        // Key 1 has freq=1, key 2 has freq=3
        // Adding new key should evict 1
        cache.put(3, new byte[1000]);

        assertNull(cache.get(1));  // Evicted (lowest frequency)
        assertNotNull(cache.get(2));  // Retained (high frequency)
        assertTrue(cache.containsKey(3));
    }

    @Test
    void testNewKeyEvictsItself() {
        // When all existing keys have higher frequency than new key
        LFUCache<Integer, Integer> cache = new LFUCache<>(3);

        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(3, 30);

        // Increase frequency of all keys
        cache.get(1); cache.get(1);  // freq(1) = 3
        cache.get(2); cache.get(2);  // freq(2) = 3
        cache.get(3); cache.get(3);  // freq(3) = 3

        // Add new key with freq=1, then another new key
        cache.put(4, 40);  // freq(4) = 1
        assertTrue(cache.containsKey(4));

        // Add another new key - should evict 4 (lowest freq=1)
        cache.put(5, 50);  // freq(5) = 1
        assertFalse(cache.containsKey(4));
        assertTrue(cache.containsKey(5));
    }
}