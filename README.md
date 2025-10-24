# LFU Cache

A generic **Least Frequently Used (LFU)** cache implementation in Java with **O(1)** time complexity for all operations.

## Overview

An efficient LFU cache that evicts the least frequently accessed items when capacity is reached. Uses **LRU as a tie-breaker** when multiple items have the same frequency.

**Key Features:**
- O(1) time complexity for get, put, and remove
- Generic implementation for any key-value types
- Frequency-based eviction with LRU tie-breaking
- 25+ comprehensive test cases

## Installation

### Build from Source

```bash
git clone <repository-url>
cd lfu
./gradlew build
./gradlew test
```

### Requirements
- Java 11+
- Gradle 7.x+

## Usage

### Basic Example

```java
LFUCache<Integer, String> cache = new LFUCache<>(3);

cache.put(1, "one");
cache.put(2, "two");
cache.put(3, "three");

cache.get(1);  // freq(1) = 2
cache.get(1);  // freq(1) = 3
cache.get(2);  // freq(2) = 2

cache.put(4, "four");  // Evicts key 3 (lowest frequency)

cache.containsKey(3);  // false (evicted)
cache.containsKey(1);  // true (high frequency)
```

### Real-World Example

```java
// API Response Cache
public class ApiCache {
    private final LFUCache<String, ApiResponse> cache;
    
    public ApiCache(int maxSize) {
        this.cache = new LFUCache<>(maxSize);
    }
    
    public ApiResponse getResponse(String endpoint) {
        ApiResponse cached = cache.get(endpoint);
        if (cached != null) return cached;
        
        ApiResponse response = fetchFromApi(endpoint);
        cache.put(endpoint, response);
        return response;
    }
}
```

## API Reference

### Constructor
```java
LFUCache(int capacity)  // Standard constructor
LFUCache(Function<Integer, Map<K, Node<K, V>>> mapFactory)  // Custom map
```

### Methods

| Method | Description | Time |
|--------|-------------|------|
| `V get(K key)` | Retrieve value, increment frequency | O(1) |
| `void put(K key, V value)` | Insert/update, increment frequency | O(1) |
| `V remove(K key)` | Remove entry | O(1) |
| `boolean containsKey(K key)` | Check existence (no frequency change) | O(1) |
| `int size()` | Current entries | O(1) |
| `int capacity()` | Max capacity | O(1) |
| `void clear()` | Remove all | O(n) |
| `boolean isEmpty()` | Check if empty | O(1) |

## Implementation

### Algorithm

Two-level data structure:
1. **HashMap** - Maps keys to nodes (O(1) lookup)
2. **Frequency List** - Doubly-linked list of frequency nodes, each containing a LinkedHashSet for LRU ordering

```
HashMap<K, Node> → FreqNode(1) ⇄ FreqNode(2) ⇄ FreqNode(3)
                       ↓              ↓              ↓
                   [k1,k2,k3]     [k4,k5]        [k6]
```

### How It Works
1. New items start at frequency 1
2. Each access increments frequency
3. Nodes move to higher frequency lists
4. Eviction removes first item from lowest frequency list
5. LinkedHashSet maintains LRU order for tie-breaking

### Complexity

**Time:** O(1) for all operations  
**Space:** O(n) where n = cache size

## Testing

```bash
# Run tests
./gradlew test

# View report
open build/reports/tests/test/index.html
```

**Test Coverage:**
- Basic frequency-based eviction
- LRU tie-breaking
- Edge cases (capacity=1, empty, null handling)
- Complex access patterns
- Large-scale operations (100+ items)

## Known Limitations

1. **Not thread-safe** - Wrap in synchronized blocks for concurrent access
2. **No TTL support** - Items don't expire based on time
3. **Memory overhead** - Additional objects per entry (Node, FreqNode)

### Thread-Safety

```java
synchronized(cache) {
    cache.put(key, value);
}
```

## References

- [LFU Paper](2110.11602v1.pdf) (included in repo)
- [Wikipedia: Cache Replacement Policies](https://en.wikipedia.org/wiki/Cache_replacement_policies#Least-frequently_used_(LFU))
- [LeetCode 460: LFU Cache](https://leetcode.com/problems/lfu-cache/)

## License

MIT License

---

**Author:** Duc Anh  
⭐ Star this repo if you find it useful!

