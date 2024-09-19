package com.midnightbits.scanner.utils.impl;

import com.midnightbits.scanner.utils.ClockInterface;

public final class SystemClock implements ClockInterface {
    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

}
