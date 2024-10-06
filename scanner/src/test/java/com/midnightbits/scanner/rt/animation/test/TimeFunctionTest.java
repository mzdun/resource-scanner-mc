// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.rt.animation.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.rt.animation.EaseFunction;
import com.midnightbits.scanner.rt.animation.ReverseFunction;
import com.midnightbits.scanner.rt.animation.TimeFunction;

public class TimeFunctionTest {
    @Test
    void easeFunctionDoesNotWork() {
        Assertions.assertEquals(0.5, EaseFunction.EASE.apply(0.5));
        Assertions.assertEquals(0.5, EaseFunction.EASE_IN.apply(0.5));
        Assertions.assertEquals(0.5, EaseFunction.EASE_OUT.apply(0.5));
        Assertions.assertEquals(0.5, EaseFunction.EASE_IN_OUT.apply(0.5));
    }

    @Test
    void reverseFunctionReverses() {
        final var tested = ReverseFunction.of(TimeFunction.LINEAR);
        Assertions.assertEquals(0.25, tested.apply(0.75));
        Assertions.assertEquals(0.5, tested.apply(0.5));
        Assertions.assertEquals(0.75, tested.apply(0.25));
    }
}
