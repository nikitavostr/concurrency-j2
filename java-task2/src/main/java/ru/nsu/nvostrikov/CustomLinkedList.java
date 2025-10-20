package ru.nsu.nvostrikov;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CustomLinkedList<T> implements Iterable<T> {

    private Element<T> head;
    private int count;

    public CustomLinkedList() {
        head = null;
        count = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Element<T> node = head;

            @Override
            public boolean hasNext() {
                return node != null;
            }

            @Override
            public T next() {
                if (node == null) {
                    throw new NoSuchElementException("No more elements");
                }
                T value = node.value;
                node = node.next;
                return value;
            }
        };
    }

    private static class Element<T> {
        T value;
        Element<T> next;
        final Object guard = new Object();

        Element(T value) {
            this.value = value;
        }
    }

    public int size() {
        return count;
    }

    public void push(T value) {
        Element<T> newElem = new Element<>(value);
        synchronized (head != null ? head.guard : this) {
            newElem.next = head;
            head = newElem;
            count++;
        }
    }

    public synchronized T get(int position) {
        if (position < 0 || position >= count) {
            throw new IndexOutOfBoundsException("Invalid index: " + position);
        }

        Element<T> temp = head;
        for (int i = 0; i < position; i++) {
            temp = temp.next;
        }
        return temp.value;
    }

    public void swap(int index) {
        if (index < 0 || index >= count - 1) {
            throw new IndexOutOfBoundsException("Invalid index for swap: " + index);
        }

        Element<T> prev = null;
        Element<T> left;
        Element<T> right;

        synchronized (this) {
            if (index == 0) {
                left = head;
                right = head.next;
            } else {
                prev = head;
                for (int i = 0; i < index - 1; i++) {
                    prev = prev.next;
                }
                left = prev.next;
                right = left.next;
            }
        }

        Object guardPrev = prev != null ? prev.guard : null;
        Object guardLeft = left.guard;
        Object guardRight = right.guard;

        if (guardPrev != null) {
            synchronized (guardPrev) {
                synchronized (guardLeft) {
                    synchronized (guardRight) {
                        performSwap(prev, left, right, false);
                    }
                }
            }
        } else {
            synchronized (guardLeft) {
                synchronized (guardRight) {
                    performSwap(null, left, right, true);
                }
            }
        }
    }

    private void performSwap(Element<T> prev, Element<T> left, Element<T> right, boolean swapHead) {
        left.next = right.next;
        right.next = left;
        if (swapHead) {
            head = right;
        } else {
            prev.next = right;
        }
    }
}
