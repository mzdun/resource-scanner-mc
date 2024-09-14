package com.midnightbits.scanner.utils.test;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.utils.CallbackIterator;

public class CallbackIteratorTest {
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
    void canSeeTheEndEdgeOfReportedItems() {
        Counter c = new Counter();
        Assertions.assertEquals(0, c.get());

        CallbackIterator<String> it = CallbackIterator.of(() -> {
            if (c.get() == 10) {
                return Optional.empty();
            }
            c.inc();
            return Optional.of("Sonar");
        });

        while (it.hasNext()) {
            String value = it.next();
            Assertions.assertEquals("Sonar", value);
        }

        Assertions.assertEquals(10, c.get());
        Assertions.assertFalse(it.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, () -> it.next());
    }
}
