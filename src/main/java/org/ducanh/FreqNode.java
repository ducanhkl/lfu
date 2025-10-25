package org.ducanh;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * FreqNode class represents a node in the frequency/time linked list.
 * Each FreqNode contains nodes with the same access time and maintains
 * doubly linked list pointers to adjacent FreqNodes.
 */
public class FreqNode <K, V> {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final int time;
    private final Set<Node<K, V>> nodes;
    private FreqNode<K, V> next;
    private FreqNode<K, V> prev;

    public FreqNode(int time, FreqNode<K, V> prev) {
        this.time = time;
        this.nodes = new LinkedHashSet<>();
        this.next = null;
        this.prev = prev;
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            next = null;
            prev = null;
            nodes.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static <K, V> void increaseFreqNode(Node<K, V> value) {
        FreqNode<K, V> currentNode = value.getFreqNode();
        currentNode.lock.writeLock().lock();
        try {
            FreqNode<K, V> freqNode = currentNode.getNextFreqNode();
            try {
                freqNode.lock.writeLock().lock();
                currentNode.nodes.remove(value);
                value.setFreqNode(freqNode);
                freqNode.nodes.add(value);
            } finally {
                freqNode.lock.writeLock().unlock();
            }
        } finally {
            currentNode.lock.writeLock().unlock();
        }
    }

    private FreqNode<K, V> getNextFreqNode() {
        if (next == null) {
            int prevTime = this.getTime();
            FreqNode<K, V> newNode = new FreqNode<>(prevTime + 1, this);
            this.setNext(newNode);
            return newNode;
        }
        return next;
    }

    public K getFirstKey() {
        lock.readLock().lock();
        try {
            return nodes.iterator().next().getKey();
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return nodes.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getTime() {
        return time;
    }

    public Set<Node<K, V>> getNodes() {
        return nodes;
    }

    public FreqNode<K, V> getNext() {
        return next;
    }

    public FreqNode<K, V> getPrev() {
        return prev;
    }

    public void addNode(Node<K, V> node) {
        lock.writeLock().lock();
        try {
            nodes.add(node);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeNode(Node<?, ?> node) {
        lock.writeLock().lock();
        try {
            nodes.remove(node);
            reduce();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void reduce() {
        if (!nodes.isEmpty() || time == 1) {
            return;
        }
        if (next != null)
            next.prev = prev;
        if (prev != null)
            prev.next = next;
    }

    public void setNext(FreqNode<K, V> next) {
        lock.writeLock().lock();
        try {
            this.next = next;
        } finally {
            lock.writeLock().unlock();
        }
    }
}

