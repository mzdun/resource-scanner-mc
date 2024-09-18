package com.midnightbits.scanner.rt.event;

@FunctionalInterface
public interface EventListener<T extends Event> {
    void apply(T event);
}
