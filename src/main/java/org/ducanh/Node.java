package org.ducanh;

import java.util.concurrent.locks.ReentrantLock;

public class Node<K, V> {
    private final ReentrantLock reentrantLock;
    private final K key;
    private V value;
    private FreqNode<K, V> freqNode;

    public Node(K key, V value, FreqNode<K, V> freqNode) {
        this.key = key;
        this.value = value;
        this.freqNode = freqNode;
        this.reentrantLock = new ReentrantLock();
    }

    public void lock() {
        reentrantLock.lock();
    }

    public void unlock() {
        reentrantLock.unlock();
    }


    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public FreqNode<K, V> getFreqNode() {
        return freqNode;
    }

    public void setFreqNode(FreqNode<K, V> freqNode) {
        this.freqNode = freqNode;
    }
}
