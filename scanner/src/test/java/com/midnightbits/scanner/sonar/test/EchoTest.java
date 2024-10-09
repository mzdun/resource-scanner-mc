// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.test;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.sonar.Echo;
import com.midnightbits.scanner.sonar.EchoState;
import com.midnightbits.scanner.sonar.graphics.Colors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EchoTest {
    public static final Colors.Proxy VANILLA = new Colors.DirectValue(Colors.VANILLA);
    public static final Colors.Proxy PURPLE = new Colors.DirectValue(Colors.PURPLE);
    public static final Colors.Proxy CLEAR = new Colors.DirectValue(Colors.CLEAR);

    private static <T> T ident(T value) {
        return value;
    }

    @Test
    void equalsTest() {
        final var echo1 = new Echo(Id.ofVanilla("iron_ore"), VANILLA);

        Assertions.assertEquals(echo1, ident(echo1));
        Assertions.assertEquals(echo1, new Echo(Id.ofVanilla("iron_ore"), VANILLA));
        Assertions.assertNotEquals(echo1, new Echo(Id.ofVanilla("iron_ore"), PURPLE));
        Assertions.assertNotEquals(echo1, new Echo(Id.ofVanilla("gold_ore"), VANILLA));
        Assertions.assertThrows(ClassCastException.class, () -> echo1.equals(EchoState.Partial.of(0, 0, 0, echo1)));
    }

    @Test
    void stringOf() {
        Assertions.assertEquals("new Echo(Id.ofVanilla(\"iron_ore\"), new Colors.DirectValue(Colors.VANILLA))",
                new Echo(Id.ofVanilla("iron_ore"), VANILLA).toString());
        Assertions.assertEquals("new Echo(Id.ofVanilla(\"iron_ore\"), new Colors.DirectValue(Colors.PURPLE))",
                new Echo(Id.ofVanilla("iron_ore"), PURPLE).toString());
        Assertions.assertEquals("new Echo(Id.ofVanilla(\"iron_ore\"), new Colors.DirectValue(0x000000))",
                new Echo(Id.ofVanilla("iron_ore"), CLEAR).toString());
        Assertions.assertEquals(
                "new Echo(Id.ofVanilla(\"iron_ore\"), Colors.BLOCK_TAG_COLORS.get(Id.ofVanilla(\"iron_ores\")))",
                new Echo(Id.ofVanilla("iron_ore"), Colors.BLOCK_TAG_COLORS.get(Id.ofVanilla("iron_ores"))).toString());
    }
}
