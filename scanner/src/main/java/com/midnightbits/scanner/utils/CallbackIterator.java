package com.midnightbits.scanner.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CallbackIterator<T> implements Iterator<T> {

    private final Callback<T> cb;
    private T nextValue;

    CallbackIterator(Callback<T> cb) {
        this.cb = cb;
        nextValue = cb.call().orElse(null);
    }

    public static <U> CallbackIterator<U> of(Callback<U> cb) {
        return new CallbackIterator<U>(cb);
    }

    @Override
    public boolean hasNext() {
        return nextValue != null;
    }

    @Override
    public T next() {
        T current = nextValue;
        if (current == null) {
            throw new NoSuchElementException("No value present");
        }
        nextValue = cb.call().orElse(null);
        return current;
    }
}
