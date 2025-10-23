package org.ducanh;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for LRUCache O(1) implementation.
 */
class LRUCacheTest {
    
    private LRUCache<Integer, String> cache;
    
    @BeforeEach
    void setUp() {
        cache = new LRUCache<>(3);
    }
    
    @Test
    void testConstructor() {
        assertEquals(3, cache.capacity());
        assertEquals(0, cache.size());
        assertTrue(cache.isEmpty());
    }
    
    @Test
    void testConstructorInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new LRUCache<>(0));
        assertThrows(IllegalArgumentException.class, () -> new LRUCache<>(-1));
    }
    
    @Test
    void testPutAndGet() {
        cache.put(1, "One");
        assertEquals("One", cache.get(1));
        assertEquals(1, cache.size());
        assertFalse(cache.isEmpty());
    }
    
    @Test
    void testGetNonExistentKey() {
        assertNull(cache.get(999));
    }
    
    @Test
    void testPutNullKey() {
        assertThrows(IllegalArgumentException.class, () -> cache.put(null, "value"));
    }
    
    @Test
    void testGetNullKey() {
        assertThrows(IllegalArgumentException.class, () -> cache.get(null));
    }
    
    @Test
    void testPutNullValue() {
        // Null values should be allowed
        cache.put(1, null);
        assertNull(cache.get(1));
        assertTrue(cache.containsKey(1));
    }
    
    @Test
    void testUpdateExistingKey() {
        cache.put(1, "One");
        cache.put(1, "ONE");
        assertEquals("ONE", cache.get(1));
        assertEquals(1, cache.size());
    }
    
    @Test
    void testEvictionPolicy() {
        cache.put(1, "One");
        cache.put(2, "Two");
        cache.put(3, "Three");
        
        // Cache is now full
        assertEquals(3, cache.size());
        
        // Add fourth element - should evict key 1 (least recently used)
        cache.put(4, "Four");
        
        assertEquals(3, cache.size());
        assertFalse(cache.containsKey(1)); // Evicted
        assertTrue(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
    }
    
    @Test
    void testEvictionWithAccess() {
        cache.put(1, "One");
        cache.put(2, "Two");
        cache.put(3, "Three");
        
        // Access key 1, making it most recently used
        cache.get(1);
        
        // Add fourth element - should evict key 2 (now least recently used)
        cache.put(4, "Four");
        
        assertTrue(cache.containsKey(1));  // Still present (was accessed)
        assertFalse(cache.containsKey(2)); // Evicted
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
    }
    
    @Test
    void testEvictionWithUpdate() {
        cache.put(1, "One");
        cache.put(2, "Two");
        cache.put(3, "Three");
        
        // Update key 1, making it most recently used
        cache.put(1, "ONE");
        
        // Add fourth element - should evict key 2
        cache.put(4, "Four");
        
        assertTrue(cache.containsKey(1));  // Still present (was updated)
        assertFalse(cache.containsKey(2)); // Evicted
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
    }
    
    @Test
    void testContainsKey() {
        cache.put(1, "One");
        assertTrue(cache.containsKey(1));
        assertFalse(cache.containsKey(2));
    }
    
    @Test
    void testRemove() {
        cache.put(1, "One");
        cache.put(2, "Two");
        
        String removed = cache.remove(1);
        assertEquals("One", removed);
        assertEquals(1, cache.size());
        assertFalse(cache.containsKey(1));
        assertTrue(cache.containsKey(2));
    }
    
    @Test
    void testRemoveNonExistent() {
        assertNull(cache.remove(999));
    }
    
    @Test
    void testRemoveNullKey() {
        assertThrows(IllegalArgumentException.class, () -> cache.remove(null));
    }
    
    @Test
    void testClear() {
        cache.put(1, "One");
        cache.put(2, "Two");
        cache.put(3, "Three");
        
        cache.clear();
        
        assertEquals(0, cache.size());
        assertTrue(cache.isEmpty());
        assertFalse(cache.containsKey(1));
        assertFalse(cache.containsKey(2));
        assertFalse(cache.containsKey(3));
    }
    
    @Test
    void testSequentialOperations() {
        // Add elements
        cache.put(1, "One");
        cache.put(2, "Two");
        cache.put(3, "Three");
        
        // Access 1 (moves to front)
        assertEquals("One", cache.get(1));
        
        // Add 4 (evicts 2)
        cache.put(4, "Four");
        assertFalse(cache.containsKey(2));
        
        // Update 3 (moves to front)
        cache.put(3, "THREE");
        
        // Add 5 (evicts 1 now)
        cache.put(5, "Five");
        assertFalse(cache.containsKey(1));
        
        // Verify final state
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
        assertTrue(cache.containsKey(5));
        assertEquals(3, cache.size());
    }
    
    @Test
    void testCapacityOne() {
        LRUCache<Integer, String> smallCache = new LRUCache<>(1);
        
        smallCache.put(1, "One");
        assertEquals("One", smallCache.get(1));
        
        smallCache.put(2, "Two");
        assertFalse(smallCache.containsKey(1));
        assertTrue(smallCache.containsKey(2));
        assertEquals(1, smallCache.size());
    }
    
    @Test
    void testLargeCapacity() {
        LRUCache<Integer, Integer> largeCache = new LRUCache<>(1000);
        
        // Fill cache
        for (int i = 0; i < 1000; i++) {
            largeCache.put(i, i * 2);
        }
        
        assertEquals(1000, largeCache.size());
        
        // Verify all elements
        for (int i = 0; i < 1000; i++) {
            assertEquals(i * 2, largeCache.get(i));
        }
        
        // Add one more - should evict key 0
        largeCache.put(1000, 2000);
        assertFalse(largeCache.containsKey(0));
        assertTrue(largeCache.containsKey(1));
        assertTrue(largeCache.containsKey(1000));
    }
    
    @Test
    void testStringKeys() {
        LRUCache<String, Integer> stringCache = new LRUCache<>(3);
        
        stringCache.put("one", 1);
        stringCache.put("two", 2);
        stringCache.put("three", 3);
        
        assertEquals(1, stringCache.get("one"));
        assertEquals(2, stringCache.get("two"));
        assertEquals(3, stringCache.get("three"));
        
        stringCache.put("four", 4);
        assertFalse(stringCache.containsKey("one"));
    }
    
    @Test
    void testComplexValueTypes() {
        LRUCache<Integer, TestObject> objectCache = new LRUCache<>(2);
        
        TestObject obj1 = new TestObject("Object1", 100);
        TestObject obj2 = new TestObject("Object2", 200);
        
        objectCache.put(1, obj1);
        objectCache.put(2, obj2);
        
        assertEquals(obj1, objectCache.get(1));
        assertEquals(obj2, objectCache.get(2));
    }
    
    @Test
    void testToString() {
        cache.put(1, "One");
        cache.put(2, "Two");
        
        String str = cache.toString();
        assertNotNull(str);
        assertTrue(str.contains("capacity=3"));
        assertTrue(str.contains("size=2"));
    }
    
    @Test
    void testMultipleGetsDoNotChangeOrder() {
        cache.put(1, "One");
        cache.put(2, "Two");
        cache.put(3, "Three");
        
        // Access key 1 multiple times
        cache.get(1);
        cache.get(1);
        cache.get(1);
        
        // Add fourth element - should still evict key 2
        cache.put(4, "Four");
        
        assertTrue(cache.containsKey(1));
        assertFalse(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
    }
    
    @Test
    void testRemoveAndReAdd() {
        cache.put(1, "One");
        cache.put(2, "Two");
        cache.put(3, "Three");
        
        cache.remove(2);
        assertEquals(2, cache.size());
        
        cache.put(2, "Two Again");
        assertEquals(3, cache.size());
        assertEquals("Two Again", cache.get(2));
    }
    
    // Helper class for testing complex value types
    private static class TestObject {
        private final String name;
        private final int value;
        
        TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestObject that = (TestObject) o;
            return value == that.value && name.equals(that.name);
        }
        
        @Override
        public int hashCode() {
            return 31 * name.hashCode() + value;
        }
    }
}

