package org.ducanh;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * FreqNode class represents a node in the frequency/time linked list.
 * Each FreqNode contains nodes with the same access time and maintains
 * doubly linked list pointers to adjacent FreqNodes.
 */
public class FreqNode <K, V> {
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

    public void reduce() {
        if (!nodes.isEmpty() || time == 1) {
            return;
        }
        if (next != null)
            next.prev = prev;
        if (prev != null)
            prev.next = next;
    }

    public void clear() {
        next = null;
        prev = null;
        nodes.clear();
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

    public FreqNode<K, V> getPrev() {
        return prev;
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

