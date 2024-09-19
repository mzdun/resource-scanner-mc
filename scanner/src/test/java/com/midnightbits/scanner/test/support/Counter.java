package com.midnightbits.scanner.test.support;

public final class Counter {
    int counter = 0;

    public void inc() {
        ++counter;
    }

    public int get() {
        return counter;
    }
}
