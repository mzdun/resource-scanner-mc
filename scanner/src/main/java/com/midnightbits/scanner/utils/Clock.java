// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils;

import com.midnightbits.scanner.utils.impl.SystemClock;

public final class Clock {
    private static ClockInterface clock = new SystemClock();

    public static void setClock(ClockInterface clock) {
        Clock.clock = clock == null ? new SystemClock() : clock;
    }

    public static long currentTimeMillis() {
        return clock.currentTimeMillis();
    }

    private Clock() {
    }
}
