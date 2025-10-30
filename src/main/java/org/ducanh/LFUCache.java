package org.ducanh;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class LFUCache<K, V> {
    private final int capacity;
    private final ConcurrentHashMap<K, Node<K, V>> map;
    private final FreqNode<K, V> headFreqNode;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Condition notFullCondition = lock.writeLock().newCondition();

    public LFUCache(int capacity) {
        ConcurrentHashMap<K, Node<K, V>> map = new ConcurrentHashMap<>(capacity);
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.capacity = capacity;
        this.map = new ConcurrentHashMap<>(capacity);
        this.headFreqNode = new FreqNode<>(1, null);
    }

    public V get(final K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        Node<K, V> node = map.get(key);
        if (node == null) {
            return null;
        }

        AtomicReference<V> result = new AtomicReference<>();
        node.executeInLock(() -> {
            if (node.state == 1) {
                return;
            }
            FreqNode<K, V> currentNode = node.getFreqNode();
            currentNode.executeInLock(() -> {
                FreqNode<K, V> nextFreqNode = currentNode.getNextFreqNode();
                nextFreqNode.executeInLock(() -> {
                    currentNode.removeNode(node);
                    node.setFreqNode(nextFreqNode);
                    nextFreqNode.getNodes().add(node);
                });
            });
            result.set(node.getValue());
        });
        return result.get();
    }

    public void put(K key, V value) {
        Objects.requireNonNull(key, "Key cannot be null");

        while (true) {
            Node<K, V> node = map.get(key);
            if (node != null) {
                AtomicInteger latestState = new AtomicInteger(0);
                node.executeInLock(() -> {
                    if (node.state == 1) {
                        latestState.set(1);
                        return;
                    }
                    while (true) {
                        AtomicBoolean isChangeSuccess = new AtomicBoolean(false);
                        FreqNode<K, V> currentNode = node.getFreqNode();
                        currentNode.executeInLock(() -> {
                            FreqNode<K, V> nextFreqNode = currentNode.getNextFreqNode();
                            nextFreqNode.executeInLock(() -> {
                                if (currentNode.state == 1 || nextFreqNode.state == 1) {
                                    return;
                                }
                                currentNode.removeNode(node);
                                node.setValue(value);
                                node.setFreqNode(nextFreqNode);
                                nextFreqNode.addNode(node);
                                isChangeSuccess.set(true);
                            });
                        });
                        if (isChangeSuccess.get()) {
                            return;
                        }
                    }
                });
                if (latestState.get() == 1) {
                    continue;
                }
                return;
            }
            lock.writeLock().lock();
            try {
                while (map.size() == capacity) {
                    // Push event to trigger eviction
                    notFullCondition.awaitUninterruptibly();
                }
                Node<K, V> refeshedNode = map.get(key);
                if (refeshedNode == null) {
                    Node<K, V> newNode = new Node<>(key, value, headFreqNode);
                    newNode.executeInLock(() -> headFreqNode.executeInLock(() -> {
                        headFreqNode.addNode(newNode);
                        map.put(key, newNode);
                    }));
                    return;
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    public void remove(K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        Node<K, V> node = map.get(key);
        if (node == null || node.state == 1) {
            return;
        }
        node.executeInLock(() -> {
            if (node.state == 1) {
                return;
            }
            FreqNode<K, V> currentNode = node.getFreqNode();
            currentNode.executeInLock(() -> {
                currentNode.removeNode(node);
                map.remove(key);
                node.state = 1;
            });
        });
        notFullCondition.signal();
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
        lock.writeLock().lock();
        try {
            map.clear();
            headFreqNode.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void evictLRU() {
        for (FreqNode<K, V> freqNode = headFreqNode; freqNode != null;) {
            for (Node<K, V> node : freqNode.getNodes()) {
                remove(node.getKey());
                if (map.size() < capacity) {
                    return;
                }
            }
            freqNode = freqNode.getNext();
        }
    }

    public void cleanEmptyFreqNode() {
        for (FreqNode<K, V> freqNode = headFreqNode; freqNode != null;) {
            
        }
    }

}
