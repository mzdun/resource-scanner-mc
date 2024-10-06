// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.test;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.sonar.BlockEcho;
import com.midnightbits.scanner.sonar.BlockEchoes;
import com.midnightbits.scanner.sonar.graphics.Colors;
import com.midnightbits.scanner.test.mocks.MockedClock;
import com.midnightbits.scanner.test.support.Iterables;

import org.junit.jupiter.api.Test;

public class BlockEchosTest {
        private MockedClock clock = new MockedClock();

        @Test
        public void removesOldEchoes() {
                final var tenSeconds = 10000;
                final var echoes = new BlockEchoes(tenSeconds);
                Iterables.assertEquals(new BlockEcho[] {}, echoes);

                clock.timeStamp = 0x123456;
                echoes.echoFrom(V3i.ZERO, Id.ofVanilla("coal_ore"), Colors.VANILLA);
                clock.timeStamp = 0x123457;
                echoes.echoFrom(new V3i(1, 1, 1), Id.ofVanilla("coal_ore"), Colors.VANILLA);
                clock.timeStamp = 0x123458;
                final var marker = echoes.echoFrom(new V3i(1, 2, 1), Id.ofVanilla("iron_ore"), Colors.VANILLA);
                clock.timeStamp = 0x123459;
                echoes.echoFrom(new V3i(1, 2, 2), Id.ofVanilla("iron_ore"), Colors.VANILLA);

                Iterables.assertEquals(new BlockEcho[] {
                                new BlockEcho(V3i.ZERO, Id.ofVanilla("coal_ore"), Colors.VANILLA, 0x123456),
                                new BlockEcho(new V3i(1, 1, 1), Id.ofVanilla("coal_ore"), Colors.VANILLA, 0x123457),
                                new BlockEcho(new V3i(1, 2, 1), Id.ofVanilla("iron_ore"), Colors.VANILLA, 0x123458),
                                new BlockEcho(new V3i(1, 2, 2), Id.ofVanilla("iron_ore"), Colors.VANILLA, 0x123459),
                }, echoes);

                clock.timeStamp = marker.pingTime() + 10000;
                echoes.removeOldEchoes();
                Iterables.assertEquals(new BlockEcho[] {
                                new BlockEcho(new V3i(1, 2, 1), Id.ofVanilla("iron_ore"), Colors.VANILLA, 0x123458),
                                new BlockEcho(new V3i(1, 2, 2), Id.ofVanilla("iron_ore"), Colors.VANILLA, 0x123459),
                }, echoes);
        }

        @Test
        public void evictsExistingEchoesWithTheSamePosition() {
                final var echoes = new BlockEchoes(BlockEchoes.ECHO_LIFETIME);
                Iterables.assertEquals(new BlockEcho[] {}, echoes);

                clock.timeStamp = 0x123456;
                echoes.echoFrom(new V3i(1, 2, 3), Id.ofVanilla("coal_ore"), Colors.VANILLA);
                Iterables.assertEquals(new BlockEcho[] {
                                new BlockEcho(new V3i(1, 2, 3), Id.ofVanilla("coal_ore"), Colors.VANILLA, 0x123456),
                }, echoes);

                clock.timeStamp = 0x123457;
                echoes.echoFrom(new V3i(1, 1, 1), Id.ofVanilla("coal_ore"), Colors.VANILLA);
                Iterables.assertEquals(new BlockEcho[] {
                                new BlockEcho(new V3i(1, 2, 3), Id.ofVanilla("coal_ore"), Colors.VANILLA, 0x123456),
                                new BlockEcho(new V3i(1, 1, 1), Id.ofVanilla("coal_ore"), Colors.VANILLA, 0x123457),
                }, echoes);

                clock.timeStamp = 0x123458;
                echoes.echoFrom(new V3i(1, 2, 3), Id.ofVanilla("iron_ore"), Colors.VANILLA);
                Iterables.assertEquals(new BlockEcho[] {
                                new BlockEcho(new V3i(1, 1, 1), Id.ofVanilla("coal_ore"), Colors.VANILLA, 0x123457),
                                new BlockEcho(new V3i(1, 2, 3), Id.ofVanilla("iron_ore"), Colors.VANILLA, 0x123458),
                }, echoes);
        }
}
