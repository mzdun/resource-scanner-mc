// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.test.support;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Assertions;

public final class Iterables {
    public static <T> void assertEquals(T[] expected, Iterable<T> actual) {
        final var actualItems = StreamSupport.stream(actual.spliterator(), false)
                .collect(Collectors.toList());

        final var expectedItems = ArrayUtils.listOf(expected);
        Assertions.assertEquals(expectedItems, actualItems);
    }

}
