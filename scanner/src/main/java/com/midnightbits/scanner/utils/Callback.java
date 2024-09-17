package com.midnightbits.scanner.utils;

import java.util.Optional;

@FunctionalInterface
public interface Callback<T> {
     Optional<T> call();
}
