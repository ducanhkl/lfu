package org.ducanh;

public class Node<K, V> {
    private final K key;
    private V value;
    private FreqNode<K, V> freqNode;

    public Node(K key, V value, FreqNode<K, V> freqNode) {
        this.key = key;
        this.value = value;
        this.freqNode = freqNode;
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
