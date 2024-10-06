// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.rt.animation;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TickSet<T> {
    private Set<T> items = new HashSet<>();

    public Set<T> copy() {
        return Set.copyOf(items);
    }

    public void add(T item) {
        items.add(item);
    }

    public void remove(T item) {
        items.remove(item);
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public boolean run(Predicate<T> predicate) {
        var copy = items;
        items = new HashSet<>();

        copy = copy.stream().filter(predicate).collect(Collectors.toSet());

        items.addAll(copy);

        return !items.isEmpty();
    }
}
