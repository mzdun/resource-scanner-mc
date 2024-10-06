// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.graphics.test;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.sonar.graphics.WaveAnimator;

public class SliceTest {
    @Test
    void emptySliceIsEmpty() {
        final var shimmers = new WaveAnimator.Slice(List.of(), List.of(), null, 0).shimmers();
        Assertions.assertEquals(0.0, shimmers.alpha());
        Assertions.assertEquals(0, shimmers.blocks().size());
    }

    @Test
    void allowsChangingAlpha() {
        final var shimmers = new WaveAnimator.Slice(List.of(), List.of(), null, 0).shimmers();
        shimmers.setAlpha(.5);
        Assertions.assertEquals(.5, shimmers.alpha());
    }
}
