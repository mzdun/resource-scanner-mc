// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.test;

import com.midnightbits.scanner.sonar.Echo;
import com.midnightbits.scanner.sonar.graphics.Colors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.sonar.BlockEcho;
import com.midnightbits.scanner.sonar.BlockEchoes;
import com.midnightbits.scanner.test.mocks.MockedClock;

public class BlockEchoTest {
        private final MockedClock clock = new MockedClock();
        public static final Colors.Proxy VANILLA = new Colors.DirectValue(Colors.VANILLA);
        public static final Colors.Proxy PURPLE = new Colors.DirectValue(Colors.PURPLE);
        public static final Colors.Proxy CLEAR = new Colors.DirectValue(Colors.CLEAR);

        final static Echo dirt = Echo.of(Id.ofVanilla("dirt"), VANILLA);
        final static Echo diamond_ore = Echo.of(Id.ofVanilla("diamond_ore"), VANILLA);
        final static Echo gold_ore = Echo.of(Id.ofVanilla("gold_ore"), VANILLA);
        final static Echo coal_ore = Echo.of(Id.ofVanilla("coal_ore"), VANILLA);
        final static Echo iron_ore = Echo.of(Id.ofVanilla("iron_ore"), VANILLA);

        @Test
        public void echoSeemsToRegisterAtGivenTime() {
                clock.timeStamp = 0x11223344;
                BlockEcho echo = BlockEcho.echoFrom(1, 2, 3, gold_ore);

                Assertions.assertEquals(echo,
                                new BlockEcho(new V3i(1, 2, 3), gold_ore, 0x11223344));
                Assertions.assertEquals(new V3i(1, 2, 3), echo.position());
                Assertions.assertEquals(Id.ofVanilla("gold_ore"), echo.id());
                Assertions.assertEquals(287454020, echo.pingTime());
                Assertions.assertEquals(
                                "\nnew BlockEcho(BlockEcho.Partial.of(1, 2, 3, new Echo(Id.ofVanilla(\"gold_ore\"), new Color.DirectValue(Colors.VANILLA))), 287454020)",
                                echo.toString());
        }

        @Test
        public void nonVanillaEchoSeemsToRegisterAtGivenTime() {
                clock.timeStamp = 0x11223344;
                BlockEcho echo = BlockEcho.echoFrom(1, 2, 3, Echo.of(Id.of("mod:gold_ore"), VANILLA));

                Assertions.assertEquals(echo,
                                new BlockEcho(new V3i(1, 2, 3), Echo.of(Id.of("mod", "gold_ore"), VANILLA), 0x11223344));
                Assertions.assertEquals(new V3i(1, 2, 3), echo.position());
                Assertions.assertEquals(Id.of("mod", "gold_ore"), echo.id());
                Assertions.assertEquals(287454020, echo.pingTime());
                Assertions.assertEquals(
                                "\nnew BlockEcho(BlockEcho.Partial.of(1, 2, 3, new Echo(Id.of(\"mod\", \"gold_ore\"), new Color.DirectValue(Colors.VANILLA))), 287454020)",
                                echo.toString());
        }

        @Test
        public void twoEchoesCompareExpectedly() {
                clock.timeStamp = 0x11223344;
                BlockEcho echo1 = BlockEcho.echoFrom(1, 2, 3, dirt);
                clock.timeStamp = 0x33445566;
                BlockEcho echo2 = BlockEcho.echoFrom(1, 2, 3, dirt);
                BlockEcho echo3 = BlockEcho.echoFrom(1, 4, 3, dirt);
                BlockEcho echo4 = BlockEcho.echoFrom(1, 4, 3, gold_ore);
                BlockEcho echo5 = BlockEcho.echoFrom(1, 4, 3, gold_ore);
                BlockEcho echo6 = BlockEcho.echoFrom(1, 4, 3, Echo.of(Id.ofVanilla("gold_ore"), CLEAR));

                Assertions.assertNotEquals(echo1, echo2);
                Assertions.assertNotEquals(echo2, echo3);
                Assertions.assertNotEquals(echo3, echo4);
                Assertions.assertEquals(echo4, echo5);
                Assertions.assertNotEquals(echo5, echo6);

                Assertions.assertTrue(echo1.compareTo(echo2) < 0);
                Assertions.assertTrue(echo2.compareTo(echo1) > 0);

                Assertions.assertTrue(echo2.compareTo(echo3) < 0);
                Assertions.assertTrue(echo3.compareTo(echo2) > 0);

                Assertions.assertTrue(echo3.compareTo(echo4) < 0);
                Assertions.assertTrue(echo4.compareTo(echo3) > 0);

                Assertions.assertEquals(0, echo4.compareTo(echo5));

                Assertions.assertTrue(echo5.compareTo(echo6) > 0);
                Assertions.assertTrue(echo6.compareTo(echo5) < 0);
        }

        @SuppressWarnings("unlikely-arg-type")
        @Test
        public void throwsWhenComparedToNonEchoes() {
                BlockEchoes blocks = new BlockEchoes();
                Assertions.assertThrows(ClassCastException.class,
                                () -> BlockEcho.echoFrom(0, 0, 0, dirt).equals(blocks));
        }

        @Test
        public void partialEchoWorks() {
                BlockEcho.Partial echo = new BlockEcho.Partial(new V3i(1, 2, 3), gold_ore);

                Assertions.assertEquals(echo, new BlockEcho.Partial(new V3i(1, 2, 3), gold_ore));
                Assertions.assertNotEquals(echo, new BlockEcho.Partial(new V3i(1, 2, 3), iron_ore));
                Assertions.assertNotEquals(echo, new BlockEcho.Partial(new V3i(2, 3, 1), gold_ore));
                Assertions.assertNotEquals(echo, new BlockEcho.Partial(new V3i(1, 2, 3), Echo.of(Id.ofVanilla("gold_ore"), PURPLE)));
                Assertions.assertEquals(new V3i(1, 2, 3), echo.position());
                Assertions.assertEquals(Id.ofVanilla("gold_ore"), echo.id());
                Assertions.assertEquals(
                        "BlockEcho.Partial.of(1, 2, 3, new Echo(Id.ofVanilla(\"gold_ore\"), new Color.DirectValue(Colors.VANILLA)))",
                        echo.toString());
                Assertions.assertEquals(
                        "BlockEcho.Partial.of(1, 2, 3, new Echo(Id.ofVanilla(\"gold_ore\"), new Color.DirectValue(Colors.PURPLE)))",
                        new BlockEcho.Partial(new V3i(1, 2, 3), Echo.of(Id.ofVanilla("gold_ore"), PURPLE)).toString());

                BlockEchoes blocks = new BlockEchoes();
                Assertions.assertThrows(ClassCastException.class,
                                () -> new BlockEcho.Partial(V3i.ZERO, dirt)
                                                .equals(blocks));
        }

        @Test
        public void nonVanillaPartialEchoWorks() {
                BlockEcho.Partial echo = new BlockEcho.Partial(new V3i(1, 2, 3), Echo.of(Id.of("mod:gold_ore"), VANILLA));

                Assertions.assertEquals(echo,
                                new BlockEcho.Partial(new V3i(1, 2, 3), Echo.of(Id.of("mod", "gold_ore"), VANILLA)));
                Assertions.assertEquals(new V3i(1, 2, 3), echo.position());
                Assertions.assertEquals(Id.of("mod", "gold_ore"), echo.id());
                Assertions.assertEquals(
                                "BlockEcho.Partial.of(1, 2, 3, new Echo(Id.of(\"mod\", \"gold_ore\"), new Color.DirectValue(Colors.VANILLA)))",
                                echo.toString());
        }
}
