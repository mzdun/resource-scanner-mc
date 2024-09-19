package com.midnightbits.scanner.test.mocks;

import com.midnightbits.scanner.utils.Clock;
import com.midnightbits.scanner.utils.ClockInterface;

public final class MockedClock implements ClockInterface {
    public long timeStamp = 0;

    public MockedClock() {
        Clock.setClock(this);
    }

    @Override
    public long currentTimeMillis() {
        return timeStamp;
    }
}
