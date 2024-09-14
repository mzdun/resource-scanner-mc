package com.midnightbits.scanner.utils;

import java.util.Iterator;
import java.util.Optional;
import java.util.NoSuchElementException;

public class CallbackIterator<T> implements Iterator<T> {

    private final Callback<T> cb;
    private Optional<T> nextValue;

    CallbackIterator(Callback<T> cb) {
        this.cb = cb;
        nextValue = cb.call();
    }

    public static <U> CallbackIterator<U> of(Callback<U> cb) {
        return new CallbackIterator<U>(cb);
    }

    @Override
    public boolean hasNext() {
        return nextValue.isPresent();
    }

    @Override
    public T next() {
        Optional<T> current = nextValue;
        if (!nextValue.isPresent()) {
            throw new NoSuchElementException("No value present");
        }
        nextValue = cb.call();
        return current.get();
    }
}
