package org.ducanh;

public class Node<K, V> {
    K key;
    V value;
    FreqNode<K, V> freqNode;

    public Node(K key, V value) {
        this.key = key;
        this.value = value;
        this.freqNode = null;
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

    public FreqNode getFreqNode() {
        return freqNode;
    }

    public void setFreqNode(FreqNode freqNode) {
        this.freqNode = freqNode;
    }
}
