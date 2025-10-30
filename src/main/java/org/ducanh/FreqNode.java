package org.ducanh;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * FreqNode class represents a node in the frequency/time linked list.
 * Each FreqNode contains nodes with the same access time and maintains
 * doubly linked list pointers to adjacent FreqNodes.
 */
public class FreqNode <K, V> {
    private final ReentrantLock lock = new ReentrantLock();
    private final int time;
    private final Set<Node<K, V>> nodes;
    private volatile FreqNode<K, V> next;
    private volatile FreqNode<K, V> prev;
    // 0 is LIVE, 1 is DELETED
    public volatile int state = 0;


    public void executeInLock(Runnable runnable) {
        lock.lock();
        try {
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    public FreqNode(int time, FreqNode<K, V> prev) {
        this.time = time;
        this.nodes = new LinkedHashSet<>();
        this.next = null;
        this.prev = prev;
    }

    public void clear() {
        lock.lock();
        try {
            next = null;
            prev = null;
            nodes.clear();
        } finally {
            lock.unlock();
        }
    }

    public FreqNode<K, V> getNextFreqNode() {
        if (next == null) {
            int prevTime = this.getTime();
            FreqNode<K, V> newNode = new FreqNode<>(prevTime + 1, this);
            next = newNode;
            return newNode;
        }
        return next;
    }

    public K getFirstKey() {
        return nodes.iterator().next().getKey();
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
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

    public void addNode(Node<K, V> node) {
        nodes.add(node);
    }

    public void removeNode(Node<?, ?> node) {
        nodes.remove(node);
    }

    public void setNext(FreqNode<K, V> next) {
        this.next = next;
    }
}

