package com.midnightbits.scanner.test.support;

import java.util.ArrayList;
import java.util.Collections;

public class ArrayUtils {
    public static <T> ArrayList<T> listOf(T[] items) {
        ArrayList<T> result = new ArrayList<>(items.length);
        Collections.addAll(result, items);
        return result;
    }
}
