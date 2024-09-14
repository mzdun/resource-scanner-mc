package com.midnightbits.scanner.utils;

import java.util.Optional;

@FunctionalInterface
public interface Callback<T> {
    public Optional<T> call();
}
