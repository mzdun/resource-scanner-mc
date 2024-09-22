package com.midnightbits.scanner.utils.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.test.mocks.MockedClock;
import com.midnightbits.scanner.utils.Clock;

public class ClockTest {
    @Test
    void clockCanHaveMockAttachedAndDetached() {
        MockedClock clock = new MockedClock();
        clock.timeStamp = 0x123456;
        Assertions.assertEquals(Clock.currentTimeMillis(), 0x123456);
        Clock.setClock(null);
        Assertions.assertNotEquals(Clock.currentTimeMillis(), 0x123456);
        Assertions.assertNotEquals(Clock.currentTimeMillis(), 0);
    }
}
