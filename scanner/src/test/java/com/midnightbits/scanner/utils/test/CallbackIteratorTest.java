// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils.test;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.test.support.Counter;
import com.midnightbits.scanner.utils.CallbackIterator;

public class CallbackIteratorTest {
    @Test
    void canSeeTheEndEdgeOfReportedItems() {
        final var c = new Counter();
        Assertions.assertEquals(0, c.get());

        final var it = CallbackIterator.of(() -> {
            if (c.get() == 10) {
                return Optional.empty();
            }
            c.inc();
            return Optional.of("Sonar");
        });

        while (it.hasNext()) {
            final var value = it.next();
            Assertions.assertEquals("Sonar", value);
        }

        Assertions.assertEquals(10, c.get());
        Assertions.assertFalse(it.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, it::next);
    }
}
