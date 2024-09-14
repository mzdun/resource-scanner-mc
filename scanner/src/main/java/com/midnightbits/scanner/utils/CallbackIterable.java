package com.midnightbits.scanner.utils;

import java.util.Iterator;

public class CallbackIterable<T> implements Iterable<T> {

    private final CallbackIterator<T> iter;

    CallbackIterable(Callback<T> cb) {
        this.iter = CallbackIterator.of(cb);
    }

    public static <U> CallbackIterable<U> of(Callback<U> cb) {
        return new CallbackIterable<U>(cb);
    }

    @Override
    public Iterator<T> iterator() {
        return iter;
    }
}
