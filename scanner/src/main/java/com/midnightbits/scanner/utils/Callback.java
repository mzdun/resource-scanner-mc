// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils;

import java.util.Optional;

@FunctionalInterface
public interface Callback<T> {
     Optional<T> call();
}
