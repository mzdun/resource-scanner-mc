// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public final class CallbackIterable<T> implements Iterable<T> {

    private final CallbackIterator<T> iter;

    CallbackIterable(Callback<T> cb) {
        this.iter = CallbackIterator.of(cb);
    }

    public static <U> CallbackIterable<U> of(Callback<U> cb) {
        return new CallbackIterable<U>(cb);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return iter;
    }
}
