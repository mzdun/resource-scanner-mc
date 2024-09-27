// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils.impl;

import com.midnightbits.scanner.utils.ClockInterface;

public final class SystemClock implements ClockInterface {
    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

}
