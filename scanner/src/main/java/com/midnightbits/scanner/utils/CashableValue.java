package com.midnightbits.scanner.utils;

import java.util.Optional;

public class CashableValue<T> {

    @FunctionalInterface
    public static interface Getter<U> {
        U get();
    };

    private Optional<T> value = Optional.empty();
    private Getter<T> getter;

    public CashableValue(Getter<T> getter) {
        this.getter = getter;
    }

    public static <U> CashableValue<U> of(Getter<U> getter) {
        return new CashableValue<U>(getter);
    }

    public T get() {
        if (value.isEmpty()) {
            value = Optional.of(getter.get());
        }

        return value.get();
    }
}
