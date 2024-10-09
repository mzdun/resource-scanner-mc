// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.graphics.test;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.sonar.graphics.ColorDefaults;
import com.midnightbits.scanner.sonar.graphics.Colors;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ColorsTest {
    private static final class ColorX implements Colors.Proxy {
        @Override
        public int rgb24() {
            return 0;
        }

        @Override
        public boolean equals(Colors.Proxy other) {
            return false;
        }

        @Override
        public int compareTo(@NotNull Colors.Proxy o) {
            return 0;
        }
    }

    private static <T> T ident(T value) {
        return value;
    }

    @Test
    void compare() {
        final var colorX = new ColorX();
        final var lapis_ores = ColorDefaults.BLOCK_TAG_COLORS.get(Id.ofVanilla("lapis_ores"));
        final var redstone_ores = ColorDefaults.BLOCK_TAG_COLORS.get(Id.ofVanilla("redstone_ores"));

        Assertions.assertTrue(lapis_ores.equals(ident(lapis_ores)));
        Assertions.assertFalse(lapis_ores.equals(redstone_ores));
        Assertions.assertFalse(lapis_ores.equals(colorX));

        Assertions.assertEquals(0, lapis_ores.compareTo(ident(lapis_ores)));
        Assertions.assertNotEquals(0, lapis_ores.compareTo(redstone_ores));
        Assertions.assertThrows(ClassCastException.class, () -> lapis_ores.compareTo(colorX));
    }
}
