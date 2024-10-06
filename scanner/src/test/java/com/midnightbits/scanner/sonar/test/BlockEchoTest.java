// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.test;

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

        @Test
        public void echoSeemsToRegisterAtGivenTime() {
                clock.timeStamp = 0x11223344;
                BlockEcho echo = BlockEcho.echoFrom(new V3i(1, 2, 3), Id.ofVanilla("gold_ore"), Colors.VANILLA);

                Assertions.assertEquals(echo,
                                new BlockEcho(new V3i(1, 2, 3), Id.ofVanilla("gold_ore"), Colors.VANILLA, 0x11223344));
                Assertions.assertEquals(new V3i(1, 2, 3), echo.position());
                Assertions.assertEquals(Id.ofVanilla("gold_ore"), echo.id());
                Assertions.assertEquals(287454020, echo.pingTime());
                Assertions.assertEquals(
                                "\nnew BlockEcho(new V3i(1, 2, 3), Id.ofVanilla(\"gold_ore\"), Colors.VANILLA, 287454020)",
                                echo.toString());
        }

        @Test
        public void nonVanillaEchoSeemsToRegisterAtGivenTime() {
                clock.timeStamp = 0x11223344;
                BlockEcho echo = BlockEcho.echoFrom(new V3i(1, 2, 3), Id.of("mod:gold_ore"), Colors.VANILLA);

                Assertions.assertEquals(echo,
                                new BlockEcho(new V3i(1, 2, 3), Id.of("mod", "gold_ore"), Colors.VANILLA, 0x11223344));
                Assertions.assertEquals(new V3i(1, 2, 3), echo.position());
                Assertions.assertEquals(Id.of("mod", "gold_ore"), echo.id());
                Assertions.assertEquals(287454020, echo.pingTime());
                Assertions.assertEquals(
                                "\nnew BlockEcho(new V3i(1, 2, 3), Id.of(\"mod\", \"gold_ore\"), Colors.VANILLA, 287454020)",
                                echo.toString());
        }

        @Test
        public void twoEchoesCompareExpectedly() {
                clock.timeStamp = 0x11223344;
                BlockEcho echo1 = BlockEcho.echoFrom(new V3i(1, 2, 3), Id.ofVanilla("dirt"), Colors.VANILLA);
                clock.timeStamp = 0x33445566;
                BlockEcho echo2 = BlockEcho.echoFrom(new V3i(1, 2, 3), Id.ofVanilla("dirt"), Colors.VANILLA);
                BlockEcho echo3 = BlockEcho.echoFrom(new V3i(1, 4, 3), Id.ofVanilla("dirt"), Colors.VANILLA);
                BlockEcho echo4 = BlockEcho.echoFrom(new V3i(1, 4, 3), Id.ofVanilla("gold_ore"), Colors.VANILLA);
                BlockEcho echo5 = BlockEcho.echoFrom(new V3i(1, 4, 3), Id.ofVanilla("gold_ore"), Colors.VANILLA);
                BlockEcho echo6 = BlockEcho.echoFrom(new V3i(1, 4, 3), Id.ofVanilla("gold_ore"), 0xFF000000);

                Assertions.assertNotEquals(echo1, echo2);
                Assertions.assertNotEquals(echo2, echo3);
                Assertions.assertNotEquals(echo3, echo4);
                Assertions.assertEquals(echo4, echo5);

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

        @Test
        public void throwsWhenComparedToNull() {
                Assertions.assertThrows(NullPointerException.class,
                                () -> BlockEcho.echoFrom(V3i.ZERO, Id.ofVanilla("dirt"), Colors.VANILLA)
                                                .equals(null));
                Assertions.assertThrows(NullPointerException.class,
                                () -> BlockEcho.echoFrom(V3i.ZERO, Id.ofVanilla("dirt"), Colors.VANILLA)
                                                .compareTo(null));
        }

        @SuppressWarnings("unlikely-arg-type")
        @Test
        public void throwsWhenComparedToNonEchoes() {
                BlockEchoes blocks = new BlockEchoes();
                Assertions.assertThrows(ClassCastException.class,
                                () -> BlockEcho.echoFrom(V3i.ZERO, Id.ofVanilla("dirt"), Colors.VANILLA)
                                                .equals(blocks));
        }

        @Test
        public void partialEchoWorks() {
                BlockEcho.Partial echo = new BlockEcho.Partial(new V3i(1, 2, 3), Id.ofVanilla("gold_ore"), Colors.VANILLA);

                Assertions.assertEquals(echo, new BlockEcho.Partial(new V3i(1, 2, 3), Id.ofVanilla("gold_ore"), Colors.VANILLA));
                Assertions.assertNotEquals(echo, new BlockEcho.Partial(new V3i(1, 2, 3), Id.ofVanilla("iron_ore"), Colors.VANILLA));
                Assertions.assertNotEquals(echo, new BlockEcho.Partial(new V3i(2, 3, 1), Id.ofVanilla("gold_ore"), Colors.VANILLA));
                Assertions.assertEquals(new V3i(1, 2, 3), echo.position());
                Assertions.assertEquals(Id.ofVanilla("gold_ore"), echo.id());
                Assertions.assertEquals(
                                "new BlockEcho.Partial(new V3i(1, 2, 3), Id.ofVanilla(\"gold_ore\"), Colors.VANILLA)",
                                echo.toString());

                BlockEchoes blocks = new BlockEchoes();
                Assertions.assertThrows(ClassCastException.class,
                                () -> new BlockEcho.Partial(V3i.ZERO, Id.ofVanilla("dirt"), Colors.VANILLA)
                                                .equals(blocks));
                Assertions.assertThrows(NullPointerException.class,
                                () -> new BlockEcho.Partial(V3i.ZERO, Id.ofVanilla("dirt"), Colors.VANILLA)
                                                .equals(null));
        }

        @Test
        public void nonVanillaPartialEchoWorks() {
                BlockEcho.Partial echo = new BlockEcho.Partial(new V3i(1, 2, 3), Id.of("mod:gold_ore"), Colors.VANILLA);

                Assertions.assertEquals(echo,
                                new BlockEcho.Partial(new V3i(1, 2, 3), Id.of("mod", "gold_ore"), Colors.VANILLA));
                Assertions.assertEquals(new V3i(1, 2, 3), echo.position());
                Assertions.assertEquals(Id.of("mod", "gold_ore"), echo.id());
                Assertions.assertEquals(
                                "new BlockEcho.Partial(new V3i(1, 2, 3), Id.of(\"mod\", \"gold_ore\"), Colors.VANILLA)",
                                echo.toString());
        }
}
