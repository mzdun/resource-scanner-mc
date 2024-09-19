package com.midnightbits.scanner.sonar.test;

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
                BlockEcho echo = BlockEcho.echoFrom(new V3i(1, 2, 3), Id.ofVanilla("gold_ore"));

                Assertions.assertEquals(echo,
                                new BlockEcho(new V3i(1, 2, 3), Id.ofVanilla("gold_ore"), 0x11223344));
                Assertions.assertEquals(new V3i(1, 2, 3), echo.position());
                Assertions.assertEquals(Id.ofVanilla("gold_ore"), echo.id());
                Assertions.assertEquals(287454020, echo.pingTime());
                Assertions.assertEquals(
                                "new BlockEcho(new V3i(1, 2, 3), Id.ofVanilla(\"gold_ore\"), 287454020)",
                                echo.toString());
        }

        @Test
        public void nonVanillaEchoSeemsToRegisterAtGivenTime() {
                clock.timeStamp = 0x11223344;
                BlockEcho echo = BlockEcho.echoFrom(new V3i(1, 2, 3), Id.of("mod:gold_ore"));

                Assertions.assertEquals(echo,
                                new BlockEcho(new V3i(1, 2, 3), Id.of("mod", "gold_ore"), 0x11223344));
                Assertions.assertEquals(new V3i(1, 2, 3), echo.position());
                Assertions.assertEquals(Id.of("mod", "gold_ore"), echo.id());
                Assertions.assertEquals(287454020, echo.pingTime());
                Assertions.assertEquals(
                                "new BlockEcho(new V3i(1, 2, 3), Id.of(\"mod\", \"gold_ore\"), 287454020)",
                                echo.toString());
        }

        @Test
        public void twoEchoesCompareExpectedly() {
                clock.timeStamp = 0x11223344;
                BlockEcho echo1 = BlockEcho.echoFrom(new V3i(1, 2, 3), Id.ofVanilla("dirt"));
                clock.timeStamp = 0x33445566;
                BlockEcho echo2 = BlockEcho.echoFrom(new V3i(1, 2, 3), Id.ofVanilla("dirt"));
                BlockEcho echo3 = BlockEcho.echoFrom(new V3i(1, 4, 3), Id.ofVanilla("dirt"));
                BlockEcho echo4 = BlockEcho.echoFrom(new V3i(1, 4, 3), Id.ofVanilla("gold_ore"));
                BlockEcho echo5 = BlockEcho.echoFrom(new V3i(1, 4, 3), Id.ofVanilla("gold_ore"));

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
        }

        @Test
        public void throwsWhenComparedToNull() {
                Assertions.assertThrows(NullPointerException.class,
                                () -> BlockEcho.echoFrom(V3i.ZERO, Id.ofVanilla("dirt"))
                                                .equals(null));
                Assertions.assertThrows(NullPointerException.class,
                                () -> BlockEcho.echoFrom(V3i.ZERO, Id.ofVanilla("dirt"))
                                                .compareTo(null));
        }

        @SuppressWarnings("unlikely-arg-type")
        @Test
        public void throwsWhenComparedToNonEchoes() {
                BlockEchoes blocks = new BlockEchoes();
                Assertions.assertThrows(ClassCastException.class,
                                () -> BlockEcho.echoFrom(V3i.ZERO, Id.ofVanilla("dirt"))
                                                .equals(blocks));
        }
}
