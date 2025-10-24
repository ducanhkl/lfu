package org.ducanh;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== LRU Cache O(1) Implementation Demo ===\n");
        
        System.out.println("Demo 1: Basic Operations");
        System.out.println("-".repeat(50));
        basicOperationsDemo();
        
        System.out.println("\n");
        
        System.out.println("Demo 2: Cache Eviction (LRU behavior)");
        System.out.println("-".repeat(50));
        evictionDemo();
        
        System.out.println("\n");
        
        System.out.println("Demo 3: Updating Existing Keys");
        System.out.println("-".repeat(50));
        updateDemo();
        
        System.out.println("\n");
        
        System.out.println("Demo 4: String Cache Example");
        System.out.println("-".repeat(50));
        stringCacheDemo();
    }
    
    private static void basicOperationsDemo() {
        LFUCache<Integer, String> cache = new LFUCache<>(3);
        
        System.out.println("Creating cache with capacity: " + cache.capacity());
        
        cache.put(1, "One");
        System.out.println("Put (1, 'One')");
        System.out.println(cache);
        
        cache.put(2, "Two");
        System.out.println("\nPut (2, 'Two')");
        System.out.println(cache);
        
        cache.put(3, "Three");
        System.out.println("\nPut (3, 'Three')");
        System.out.println(cache);
        
        System.out.println("\nGet key 1: " + cache.get(1));
        System.out.println("After accessing key 1:");
        System.out.println(cache);
    }
    
    private static void evictionDemo() {
        LFUCache<Integer, String> cache = new LFUCache<>(3);
        
        cache.put(1, "One");
        cache.put(2, "Two");
        cache.put(3, "Three");
        System.out.println("Initial cache (capacity 3):");
        System.out.println(cache);
        
        System.out.println("\nAdding (4, 'Four') - should evict key 1 (LRU)");
        cache.put(4, "Four");
        System.out.println(cache);
        
        System.out.println("\nAccessing key 2 (moves it to front)");
        cache.get(2);
        System.out.println(cache);
        
        System.out.println("\nAdding (5, 'Five') - should evict key 3 (new LRU)");
        cache.put(5, "Five");
        System.out.println(cache);
        
        System.out.println("\nChecking if key 1 exists: " + cache.containsKey(1));
        System.out.println("Checking if key 3 exists: " + cache.containsKey(3));
        System.out.println("Checking if key 2 exists: " + cache.containsKey(2));
    }
    
    private static void updateDemo() {
        LFUCache<Integer, String> cache = new LFUCache<>(3);
        
        cache.put(1, "One");
        cache.put(2, "Two");
        cache.put(3, "Three");
        System.out.println("Initial cache:");
        System.out.println(cache);
        
        System.out.println("\nUpdating key 1 from 'One' to 'ONE'");
        cache.put(1, "ONE");
        System.out.println(cache);
        
        System.out.println("\nAdding new key 4");
        cache.put(4, "Four");
        System.out.println("Key 2 should be evicted (it was LRU):");
        System.out.println(cache);
    }
    
    private static void stringCacheDemo() {
        LFUCache<String, Integer> cache = new LFUCache<>(4);
        
        System.out.println("Creating a cache for word counts:");
        cache.put("hello", 5);
        cache.put("world", 5);
        cache.put("java", 4);
        cache.put("cache", 5);
        System.out.println(cache);
        
        System.out.println("\nAccessing 'java':");
        Integer count = cache.get("java");
        System.out.println("Count for 'java': " + count);
        System.out.println(cache);
        
        System.out.println("\nAdding 'programming' - should evict 'hello':");
        cache.put("programming", 11);
        System.out.println(cache);
        
        System.out.println("\nRemoving 'world':");
        cache.remove("world");
        System.out.println(cache);
        System.out.println("Current size: " + cache.size());
    }
}
