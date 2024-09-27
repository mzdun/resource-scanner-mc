// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils;

public final class CacheableValue<T> {

    @FunctionalInterface
    public static interface Getter<U> {
        U get();
    };

    private T value = null;
    private final Getter<T> getter;

    public CacheableValue(Getter<T> getter) {
        this.getter = getter;
    }

    public static <U> CacheableValue<U> of(Getter<U> getter) {
        return new CacheableValue<U>(getter);
    }

    public T get() {
        if (value == null) {
            value = getter.get();
        }

        return value;
    }
}
