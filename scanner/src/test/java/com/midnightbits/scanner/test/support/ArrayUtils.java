package com.midnightbits.scanner.test.support;

import java.util.ArrayList;

public class ArrayUtils {
    public static <T> ArrayList<T> listOf(T[] items) {
        ArrayList<T> result = new ArrayList<>(items.length);
        for (T item : items)
            result.add(item);
        return result;
    }
}
