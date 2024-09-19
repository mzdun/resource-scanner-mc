package com.midnightbits.scanner.test.support;

import java.util.ArrayList;
import java.util.Collections;

public final class ArrayUtils {
    public static <T> ArrayList<T> listOf(T[] items) {
        final var result = new ArrayList<T>(items.length);
        Collections.addAll(result, items);
        return result;
    }
}
