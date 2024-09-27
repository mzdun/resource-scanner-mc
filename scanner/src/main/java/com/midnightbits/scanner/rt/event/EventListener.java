// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.rt.event;

@FunctionalInterface
public interface EventListener<T extends Event> {
    void apply(T event);
}
