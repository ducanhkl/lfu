package org.ducanh;

import java.util.*;
import java.util.function.Function;

public class LRUCache<K, V> {
    private final int capacity;
    private final Map<K, Node<K, V>> map;
    private final FreqNode<K, V> headFreqNode;

    public LRUCache(Function<Integer, Map<K, Node<K, V>>> mapFactory) {
        if (mapFactory == null) {
            throw new IllegalArgumentException("Map factory cannot be null");
        }
        this.capacity = 0;
        this.map = mapFactory.apply(16);
        this.headFreqNode = new FreqNode<>(1, null);
    }

    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.capacity = capacity;
        this.map = new HashMap<>(capacity);
        this.headFreqNode = new FreqNode<>(1, null);
    }

    public V get(final K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        return Optional.ofNullable(map.get(key))
                .stream().peek(this::increaseNodeTime)
                .map(Node::getValue)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public void put(K key, V value) {
        evictLRU(1);
        Objects.requireNonNull(key, "Key cannot be null");
        
        Node<K, V> node = map.get(key);

        if (node != null) {
            node.value = value;
            increaseNodeTime(node);
            return;
        }

        Node<K, V> newNode = new Node<>(key, value, headFreqNode);
        map.put(key, newNode);
        headFreqNode.addNode(newNode);

    }

    public V remove(K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        
        Node<K, V> node = map.get(key);
        if (node == null) {
            return null;
        }

        removeNodeFromFreqTree(node);
        map.remove(key);
        return node.value;
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public int size() {
        return map.size();
    }

    public int capacity() {
        return capacity;
    }

    public void clear() {
        map.clear();
        headFreqNode.clear();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    private void increaseNodeTime(Node<K, V> node) {
        FreqNode<K, V> preFreqNode = node.freqNode;
        removeNodeFromFreqTree(node);
        FreqNode<K, V> nextFreqNode = getNextFreqNode(preFreqNode);
        nextFreqNode.addNode(node);
    }

    private FreqNode<K, V> getNextFreqNode(final FreqNode<K, V>preFreqNode) {
        final FreqNode<K, V>nextFreqNode = preFreqNode.getNext();
        if (preFreqNode.getNext() == null) {
            int prevTime = preFreqNode.getTime();
            FreqNode<K, V> newNode = new FreqNode<>(prevTime + 1, preFreqNode);
            preFreqNode.setNext(newNode);
            return newNode;
        }
        return nextFreqNode;
    }

    private void removeNodeFromFreqTree(final Node<K, V> node) {
        final FreqNode<K, V> freqNode = node.freqNode;
        freqNode.getNodes().remove(node);
        freqNode.reduce();
    }

    private void evictLRU(final int buffer) {
        assert headFreqNode.getTime() == 1;
        while (map.size() > capacity - buffer && !headFreqNode.isEmpty()) {
            K shouldRemove = headFreqNode.getFirstKey();
            remove(shouldRemove);
        }
        FreqNode<K, V> nextFreqNode = headFreqNode.getNext();
        assert nextFreqNode == null || nextFreqNode.getTime() > 1;
        while (map.size() > capacity - buffer && nextFreqNode != null && !nextFreqNode.isEmpty()) {
            K shouldRemove = nextFreqNode.getFirstKey();
            remove(shouldRemove);
        }
    }
}
