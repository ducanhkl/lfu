package org.ducanh;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LRUCacheTest {
    
    @Test
    void testConstructor() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);
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
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        cache.put(1, "One");
        assertEquals("One", cache.get(1));
        assertEquals(1, cache.size());
        assertFalse(cache.isEmpty());
    }
    
    @Test
    void testGetNonExistentKey() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        assertNull(cache.get(999));
    }
    
    @Test
    void testPutNullKey() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        assertThrows(NullPointerException.class, () -> cache.put(null, "value"));
    }
    
    @Test
    void testGetNullKey() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        assertThrows(NullPointerException.class, () -> cache.get(null));
    }
    
    @Test
    void testPutNullValue() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        cache.put(1, null);
        assertNull(cache.get(1));
        assertTrue(cache.containsKey(1));
    }
    
    @Test
    void testUpdateExistingKey() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        cache.put(1, "One");
        cache.put(1, "ONE");
        assertEquals("ONE", cache.get(1));
        assertEquals(1, cache.size());
    }
    
    @Test
    void testEvictionPolicy() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        cache.put(1, "One");
        cache.put(2, "Two");
        cache.put(3, "Three");
        
        assertEquals(3, cache.size());
        
        cache.put(4, "Four");
        
        assertEquals(3, cache.size());
        assertFalse(cache.containsKey(1));
        assertTrue(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
    }
    
    @Test
    void testEvictionWithAccess() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        cache.put(1, "One");
        cache.put(2, "Two");
        cache.put(3, "Three");
        
        cache.get(1);
        
        cache.put(4, "Four");
        
        assertTrue(cache.containsKey(1));
        assertFalse(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
    }
    
    @Test
    void testEvictionWithUpdate() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        cache.put(1, "One");
        cache.put(2, "Two");
        cache.put(3, "Three");
        
        cache.put(1, "ONE");
        
        cache.put(4, "Four");
        
        assertTrue(cache.containsKey(1));
        assertFalse(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
    }
    
    @Test
    void testContainsKey() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        cache.put(1, "One");
        assertTrue(cache.containsKey(1));
        assertFalse(cache.containsKey(2));
    }
    
    @Test
    void testRemove() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);
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
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        assertNull(cache.remove(999));
    }
    
    @Test
    void testRemoveNullKey() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        assertThrows(NullPointerException.class, () -> cache.remove(null));
    }
    
    @Test
    void testClear() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);
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
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        cache.put(1, "One");
        cache.put(2, "Two");
        cache.put(3, "Three");
        
        assertEquals("One", cache.get(1));
        
        cache.put(4, "Four");
        assertFalse(cache.containsKey(2));
        
        cache.put(3, "THREE");
        
        cache.put(5, "Five");
        assertFalse(cache.containsKey(4));
        
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(3));
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
        
        for (int i = 0; i < 1000; i++) {
            largeCache.put(i, i * 2);
        }
        
        assertEquals(1000, largeCache.size());
        
        for (int i = 0; i < 1000; i++) {
            assertEquals(i * 2, largeCache.get(i));
        }
        
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
    void testMultipleGetsDoNotChangeOrder() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        cache.put(1, "One");
        cache.put(2, "Two");
        cache.put(3, "Three");
        
        cache.get(1);
        cache.get(1);
        cache.get(1);
        
        cache.put(4, "Four");
        
        assertTrue(cache.containsKey(1));
        assertFalse(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
    }
    
    @Test
    void testRemoveAndReAdd() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        cache.put(1, "One");
        cache.put(2, "Two");
        cache.put(3, "Three");
        
        cache.remove(2);
        assertEquals(2, cache.size());
        
        cache.put(2, "Two Again");
        assertEquals(3, cache.size());
        assertEquals("Two Again", cache.get(2));
    }
    
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
