// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.test;

import com.midnightbits.scanner.sonar.Echo;
import com.midnightbits.scanner.sonar.EchoState;
import com.midnightbits.scanner.sonar.graphics.Colors;
import com.midnightbits.scanner.sonar.graphics.Pixel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.sonar.Echoes;
import com.midnightbits.scanner.test.mocks.MockedClock;

public class EchoStateTest {
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
                EchoState echo = EchoState.echoFrom(1, 2, 3, gold_ore);

                Assertions.assertEquals(echo,
                                new EchoState(new V3i(1, 2, 3), gold_ore, 0x11223344));
                Assertions.assertEquals(new V3i(1, 2, 3), echo.position());
                Assertions.assertEquals(Id.ofVanilla("gold_ore"), echo.id());
                Assertions.assertEquals(287454020, echo.pingTime());
                Assertions.assertEquals(
                                "\nnew EchoState(1, 2, 3, new Echo(Id.ofVanilla(\"gold_ore\"), new Colors.DirectValue(Colors.VANILLA)), 287454020)",
                                echo.toString());
        }

        @Test
        public void nonVanillaEchoSeemsToRegisterAtGivenTime() {
                clock.timeStamp = 0x11223344;
                EchoState echo = EchoState.echoFrom(1, 2, 3, Echo.of(Id.of("mod:gold_ore"), VANILLA));

                Assertions.assertEquals(echo,
                        new EchoState(new V3i(1, 2, 3), Echo.of(Id.of("mod", "gold_ore"), VANILLA), 0x11223344));
                Assertions.assertEquals(new V3i(1, 2, 3), echo.position());
                Assertions.assertEquals(Id.of("mod", "gold_ore"), echo.id());
                Assertions.assertEquals(287454020, echo.pingTime());
                Assertions.assertEquals(Pixel.ALL_SIDES, echo.sides);
                Assertions.assertEquals(0, echo.edges);
                Assertions.assertEquals(Colors.ECHO_ALPHA, echo.alpha);
                Assertions.assertEquals(
                        "\nnew EchoState(1, 2, 3, new Echo(Id.of(\"mod\", \"gold_ore\"), new Colors.DirectValue(Colors.VANILLA)), 287454020)",
                        echo.toString());
        }

        @Test
        public void toStringTest() {
                clock.timeStamp = 0x11223344;
                EchoState echo = EchoState.echoFrom(1, 2, 3, Echo.of(Id.ofVanilla("gold_ore"), VANILLA)).withAllEdges();


                Assertions.assertEquals(Pixel.ALL_EDGES, echo.edges);
                Assertions.assertEquals(
                        "\nnew EchoState(1, 2, 3, new Echo(Id.ofVanilla(\"gold_ore\"), new Colors.DirectValue(Colors.VANILLA)), 287454020).withAllEdges()",
                        echo.toString());

                echo.alpha = Colors.OPAQUE;
                Assertions.assertEquals(
                        "\nnew EchoState(1, 2, 3, new Echo(Id.ofVanilla(\"gold_ore\"), new Colors.DirectValue(Colors.VANILLA)), 287454020, Pixel.ALL_SIDES, Pixel.ALL_EDGES, 0xFF000000)",
                        echo.toString());

                echo.alpha = Colors.ECHO_ALPHA;
                echo.edges &= ~Pixel.EDGE_LEFT_TOP;
                Assertions.assertEquals(
                        "\nnew EchoState(1, 2, 3, new Echo(Id.ofVanilla(\"gold_ore\"), new Colors.DirectValue(Colors.VANILLA)), 287454020, Pixel.ALL_SIDES, 0xdff, Colors.ECHO_ALPHA)",
                        echo.toString());

                echo.sides &= ~Pixel.SIDE_X1;
                Assertions.assertEquals(
                        "\nnew EchoState(1, 2, 3, new Echo(Id.ofVanilla(\"gold_ore\"), new Colors.DirectValue(Colors.VANILLA)), 287454020, 0x1f, 0xdff, Colors.ECHO_ALPHA)",
                        echo.toString());
        }

        @Test
        public void twoEchoesCompareExpectedly() {
                clock.timeStamp = 0x11223344;
                EchoState echo1 = EchoState.echoFrom(1, 2, 3, dirt);
                clock.timeStamp = 0x33445566;
                EchoState echo2 = EchoState.echoFrom(1, 2, 3, dirt);
                EchoState echo3 = EchoState.echoFrom(1, 4, 3, dirt);
                EchoState echo4 = EchoState.echoFrom(1, 4, 3, gold_ore);
                EchoState echo5 = EchoState.echoFrom(1, 4, 3, gold_ore);
                EchoState echo6 = EchoState.echoFrom(1, 4, 3, Echo.of(Id.ofVanilla("gold_ore"), CLEAR));

                EchoState echo2A = EchoState.echoFrom(1, 2, 3, dirt).withAllEdges();
                EchoState echo2B = EchoState.echoFrom(1, 2, 3, dirt);
                EchoState echo2C = EchoState.echoFrom(1, 2, 3, dirt);
                echo2B.sides = 0;
                echo2C.alpha = Colors.OPAQUE;

                Assertions.assertNotEquals(echo1, echo2);
                Assertions.assertNotEquals(echo2, echo3);
                Assertions.assertNotEquals(echo3, echo4);
                Assertions.assertEquals(echo4, echo5);
                Assertions.assertNotEquals(echo5, echo6);

                Assertions.assertNotEquals(echo2, echo2A);
                Assertions.assertNotEquals(echo2, echo2B);
                Assertions.assertNotEquals(echo2, echo2C);

                Assertions.assertTrue(echo1.compareTo(echo2) < 0);
                Assertions.assertTrue(echo2.compareTo(echo1) > 0);

                Assertions.assertTrue(echo2.compareTo(echo3) < 0);
                Assertions.assertTrue(echo3.compareTo(echo2) > 0);

                Assertions.assertTrue(echo3.compareTo(echo4) < 0);
                Assertions.assertTrue(echo4.compareTo(echo3) > 0);

                Assertions.assertEquals(0, echo4.compareTo(echo5));

                Assertions.assertTrue(echo5.compareTo(echo6) > 0);
                Assertions.assertTrue(echo6.compareTo(echo5) < 0);

                Assertions.assertTrue(echo2.compareTo(echo2A) < 0);
                Assertions.assertTrue(echo2.compareTo(echo2B) > 0);
                Assertions.assertTrue(echo2.compareTo(echo2C) < 0);
        }

        @SuppressWarnings("unlikely-arg-type")
        @Test
        public void throwsWhenComparedToNonEchoes() {
                Echoes blocks = new Echoes();
                Assertions.assertThrows(ClassCastException.class,
                                () -> EchoState.echoFrom(0, 0, 0, dirt).equals(blocks));
        }

        @Test
        public void partialEchoWorks() {
                EchoState.Partial echo = new EchoState.Partial(new V3i(1, 2, 3), gold_ore);

                Assertions.assertEquals(echo, new EchoState.Partial(new V3i(1, 2, 3), gold_ore));
                Assertions.assertNotEquals(echo, new EchoState.Partial(new V3i(1, 2, 3), iron_ore));
                Assertions.assertNotEquals(echo, new EchoState.Partial(new V3i(2, 3, 1), gold_ore));
                Assertions.assertNotEquals(echo, new EchoState.Partial(new V3i(1, 2, 3), Echo.of(Id.ofVanilla("gold_ore"), PURPLE)));
                Assertions.assertEquals(new V3i(1, 2, 3), echo.position());
                Assertions.assertEquals(Id.ofVanilla("gold_ore"), echo.id());
                Assertions.assertEquals(
                        "EchoState.Partial.of(1, 2, 3, new Echo(Id.ofVanilla(\"gold_ore\"), new Colors.DirectValue(Colors.VANILLA)))",
                        echo.toString());
                Assertions.assertEquals(
                        "EchoState.Partial.of(1, 2, 3, new Echo(Id.ofVanilla(\"gold_ore\"), new Colors.DirectValue(Colors.PURPLE)))",
                        new EchoState.Partial(new V3i(1, 2, 3), Echo.of(Id.ofVanilla("gold_ore"), PURPLE)).toString());

                Echoes blocks = new Echoes();
                Assertions.assertThrows(ClassCastException.class,
                                () -> new EchoState.Partial(V3i.ZERO, dirt)
                                                .equals(blocks));
        }

        @Test
        public void nonVanillaPartialEchoWorks() {
                EchoState.Partial echo = new EchoState.Partial(new V3i(1, 2, 3), Echo.of(Id.of("mod:gold_ore"), VANILLA));

                Assertions.assertEquals(echo,
                                new EchoState.Partial(new V3i(1, 2, 3), Echo.of(Id.of("mod", "gold_ore"), VANILLA)));
                Assertions.assertEquals(new V3i(1, 2, 3), echo.position());
                Assertions.assertEquals(Id.of("mod", "gold_ore"), echo.id());
                Assertions.assertEquals(
                                "EchoState.Partial.of(1, 2, 3, new Echo(Id.of(\"mod\", \"gold_ore\"), new Colors.DirectValue(Colors.VANILLA)))",
                                echo.toString());
        }
}
