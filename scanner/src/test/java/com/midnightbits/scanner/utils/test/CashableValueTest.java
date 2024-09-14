package com.midnightbits.scanner.utils.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.utils.CashableValue;

public class CashableValueTest {
    private static class Counter {
        int counter = 0;

        public Counter() {
        }

        public void inc() {
            ++counter;
        }

        public int get() {
            return counter;
        }
    };

    @Test
    void itOnlyCallsGetterOnce() {
        Counter c = new Counter();

        CashableValue<Integer> cached = CashableValue.of(() -> {
            c.inc();
            return 42;
        });

        Assertions.assertEquals(42, cached.get());
        Assertions.assertEquals(42, cached.get());
        Assertions.assertEquals(42, cached.get());
        Assertions.assertEquals(1, c.get());
    }
}
