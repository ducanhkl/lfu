package org.ducanh;

import java.util.*;
import java.util.function.Function;

public class LFUCache<K, V> {
    private final int capacity;
    private final Map<K, Node<K, V>> map;
    private final FreqNode<K, V> headFreqNode;

    public LFUCache(Function<Integer, Map<K, Node<K, V>>> mapFactory) {
        if (mapFactory == null) {
            throw new IllegalArgumentException("Map factory cannot be null");
        }
        this.capacity = 0;
        this.map = mapFactory.apply(16);
        this.headFreqNode = new FreqNode<>(1, null);
    }

    public LFUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.capacity = capacity;
        this.map = new HashMap<>(capacity);
        this.headFreqNode = new FreqNode<>(1, null);
    }

    public V get(final K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        Node<K, V> node = map.get(key);
        if (node == null) {
            return null;
        }
        try {
            node.lock();
            FreqNode.increaseFreqNode(node);
            return node.getValue();
        } finally {
            node.unlock();
        }
    }

    public void put(K key, V value) {
        Objects.requireNonNull(key, "Key cannot be null");

        Node<K, V> node = map.get(key);

        if (node != null) {
            node.lock();
            try {
                node.setValue(value);
                FreqNode.increaseFreqNode(node);
            } finally {
                node.unlock();
            }
            return;
        }

        synchronized (map) {
            evictLRU(1);
            Node<K, V> newNode = new Node<>(key, value, headFreqNode);
            newNode.lock();
            try {
                map.put(key, newNode);
                headFreqNode.addNode(newNode);
            } finally {
                newNode.unlock();
            }
        }
    }

    public V remove(K key) {
        Objects.requireNonNull(key, "Key cannot be null");

        Node<K, V> node = map.get(key);
        if (node == null) {
            return null;
        }

        node.lock();
        try {
            removeNodeFromFreqTree(node);
            map.remove(key);
            return node.getValue();
        } finally {
            node.unlock();
        }

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

    public synchronized void clear() {
        map.clear();
        headFreqNode.clear();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    private void removeNodeFromFreqTree(final Node<K, V> node) {
        final FreqNode<K, V> freqNode = node.getFreqNode();
        freqNode.removeNode(node);
    }

    private void evictLRU(final int buffer) {
        assert headFreqNode.getTime() == 1;
        while (map.size() > capacity - buffer && !headFreqNode.isEmpty()) {
            K shouldRemove = headFreqNode.getFirstKey();
            remove(shouldRemove);
        }
        FreqNode<K, V> nextFreqNode = headFreqNode.getNext();
        assert nextFreqNode == null || nextFreqNode.getTime() > 1;
        while (map.size() > capacity - buffer && nextFreqNode != null) {
            if (!nextFreqNode.isEmpty()) {
                K shouldRemove = nextFreqNode.getFirstKey();
                remove(shouldRemove);
                return;
            }
            nextFreqNode = nextFreqNode.getNext();
        }
    }
}
