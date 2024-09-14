package com.midnightbits.scanner.test.support;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Assertions;

public class Iterables {
    public static <T> void assertEquals(T[] expected, Iterable<T> actual) {
        List<T> actualItems = StreamSupport.stream(actual.spliterator(), false)
                .collect(Collectors.toList());

        ArrayList<T> expectedItems = ArrayUtils.listOf(expected);
        Assertions.assertEquals(expectedItems, actualItems);
    }

}
