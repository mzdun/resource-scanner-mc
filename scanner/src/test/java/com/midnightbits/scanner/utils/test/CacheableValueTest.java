package com.midnightbits.scanner.utils.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.test.support.Counter;
import com.midnightbits.scanner.utils.CacheableValue;

public class CacheableValueTest {
    @Test
    void itOnlyCallsGetterOnce() {
        final var c = new Counter();

        CacheableValue<Integer> cached = CacheableValue.of(() -> {
            c.inc();
            return 42;
        });

        Assertions.assertEquals(42, cached.get());
        Assertions.assertEquals(42, cached.get());
        Assertions.assertEquals(42, cached.get());
        Assertions.assertEquals(1, c.get());
    }
}
