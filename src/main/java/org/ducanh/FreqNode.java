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

    public static <K, V> void increaseFreq(Node<K, V> kVNode) {
        kVNode.lock();
        FreqNode<K, V> currentNode = kVNode.getFreqNode();
        currentNode.lock.writeLock().lock();
        try {
            FreqNode<K, V> freqNode = currentNode.getNextFreqNode();
            try {
                freqNode.lock.writeLock().lock();
                currentNode.nodes.remove(kVNode);
                kVNode.setFreqNode(freqNode);
                freqNode.nodes.add(kVNode);
            } finally {
                freqNode.lock.writeLock().unlock();
            }
        } finally {
            kVNode.unlock();
            currentNode.lock.writeLock().unlock();
        }
    }

    public static <K, V> void setNewValueAndIncreaseFreqNode(Node<K, V> kVNode, V newValue) {
        kVNode.lock();
        FreqNode<K, V> currentNode = kVNode.getFreqNode();
        currentNode.lock.writeLock().lock();
        try {
            FreqNode<K, V> freqNode = currentNode.getNextFreqNode();
            try {
                freqNode.lock.writeLock().lock();
                currentNode.nodes.remove(kVNode);
                kVNode.setFreqNode(freqNode);
                kVNode.setValue(newValue);
                freqNode.nodes.add(kVNode);
            } finally {
                freqNode.lock.writeLock().unlock();
            }
        } finally {
            kVNode.unlock();
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

    public void addNode(Node<K, V> node, Runnable safeExecute) {
        node.lock();
        lock.writeLock().lock();
        try {
            nodes.add(node);
            safeExecute.run();
        } finally {
            lock.writeLock().unlock();
            node.unlock();
        }
    }

    public void removeNode(Node<?, ?> node) {
        node.lock();
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

