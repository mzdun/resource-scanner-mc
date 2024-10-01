// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.test;

import java.util.Set;

import com.midnightbits.scanner.sonar.BlockEchoes;
import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.sonar.BlockEcho;
import com.midnightbits.scanner.sonar.Sonar;
import com.midnightbits.scanner.test.mocks.MockClientCore;
import com.midnightbits.scanner.test.mocks.MockWorld;
import com.midnightbits.scanner.test.mocks.MockedClock;
import com.midnightbits.scanner.test.support.Iterables;

public class SonarTest {
        public final static int TEST_BLOCK_DISTANCE = 32;
        public final static int TEST_BLOCK_RADIUS = 4;
        public static Id[] TEST_INTERESTING_IDS = new Id[] {
                        Id.ofVanilla("coal_ore"),
                        Id.ofVanilla("deepslate_coal_ore"),
                        Id.ofVanilla("iron_ore"),
                        Id.ofVanilla("deepslate_iron_ore"),
                        Id.ofVanilla("diamond_ore"),
                        Id.ofVanilla("deepslate_diamond_ore"),
                        Id.ofVanilla("netherite_block"),
        };
        private final MockedClock clock = new MockedClock();
        private final static MockWorld TEST_WORLD = MockWorld.ofResource("test_world.txt");

        public static Sonar narrowSonar() {
                return narrowSonar(TEST_BLOCK_DISTANCE, Set.of(TEST_INTERESTING_IDS));
        }

        public static Sonar narrowSonar(int blockDistance, Set<Id> blocks) {
                return new Sonar(blockDistance, 0, blocks, BlockEchoes.MAX_SIZE);
        }

        @Test
        void checkNewSonarIsEmpty() {
                final var empty = new Sonar();
                Iterables.assertEquals(new BlockEcho[] {}, empty.echoes());
        }

        @Test
        void checkDownwardsDirectionFromMiddle_narrow() {
                final var core = new MockClientCore(V3i.ZERO, -90, 0, TEST_WORLD);

                clock.timeStamp = 0x123456;
                final var sonar = narrowSonar();
                sonar.ping(core);

                Iterables.assertEquals(new BlockEcho[] {
                                new BlockEcho(new V3i(0, 25, 0), Id.ofVanilla("deepslate_diamond_ore"), 0x123456),
                                new BlockEcho(new V3i(0, 23, 0), Id.ofVanilla("deepslate_iron_ore"), 0x123456),
                                new BlockEcho(new V3i(0, 27, 0), Id.ofVanilla("diamond_ore"), 0x123456),
                                new BlockEcho(new V3i(0, 28, 0), Id.ofVanilla("iron_ore"), 0x123456),
                                new BlockEcho(new V3i(0, 30, 0), Id.ofVanilla("iron_ore"), 0x123456),
                }, sonar.echoes());

                Iterables.assertEquals(new String[] {
                                "> 23m deepslate_iron_ore",
                                "> 25m deepslate_diamond_ore",
                                "> 27m diamond_ore",
                                "> 28m iron_ore",
                                "> 30m iron_ore",
                }, core.getPlayerMessages());
        }

        @Test
        void checkDownwardsDirectionFromMiddleButOnlyDiamonds_narrow() {
                final var core = new MockClientCore(V3i.ZERO, -90f, 0f, TEST_WORLD);

                clock.timeStamp = 0x123456;
                final var sonar = narrowSonar(TEST_BLOCK_DISTANCE, Set.of(
                                Id.ofVanilla("diamond_ore"),
                                Id.ofVanilla("deepslate_diamond_ore")));
                sonar.ping(core);

                Iterables.assertEquals(new BlockEcho[] {
                                new BlockEcho(new V3i(0, 25, 0), Id.ofVanilla("deepslate_diamond_ore"), 1193046),
                                new BlockEcho(new V3i(0, 27, 0), Id.ofVanilla("diamond_ore"), 1193046),
                }, sonar.echoes());
        }

        @Test
        void searchForGold() {
                final var core = new MockClientCore(new V3i(-60, -60, -51), 0f, 0f, TEST_WORLD);

                clock.timeStamp = 0x123456;
                final var sonar = new Sonar(TEST_BLOCK_DISTANCE, TEST_BLOCK_RADIUS, Set.of(Id.ofVanilla("gold_ore")));
                sonar.ping(core);

                Iterables.assertEquals(new BlockEcho[] {
                                new BlockEcho(new V3i(-60, -60, -50), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-60, -60, -33), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-59, -60, -29), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-57, -60, -28), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-57, -60, -27), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-57, -60, -26), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-58, -60, -25), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-58, -60, -24), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-59, -60, -22), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-60, -59, -39), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-58, -59, -39), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-60, -59, -37), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-59, -59, -35), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-59, -59, -34), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-57, -59, -30), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-58, -59, -26), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-60, -59, -23), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-56, -59, -20), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-56, -59, -19), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-60, -58, -34), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-59, -58, -25), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-60, -58, -23), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-56, -58, -23), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-58, -58, -22), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-59, -58, -21), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-57, -58, -21), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-60, -57, -27), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-59, -57, -26), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-59, -57, -25), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-59, -57, -22), Id.ofVanilla("gold_ore"), 1193046),
                                new BlockEcho(new V3i(-59, -56, -19), Id.ofVanilla("gold_ore"), 1193046),
                }, sonar.echoes());

                Iterables.assertEquals(new String[] {
                                "> 1m gold_ore", "> 18m gold_ore", "> 22m gold_ore", "> 29m gold_ore", "> 26m gold_ore",
                                "> 27m gold_ore", "> 23m gold_ore", "> 24m gold_ore", "> 25m gold_ore",
                                "> 28m gold_ore", "> 16m gold_ore", "> 17m gold_ore", "> 25m gold_ore",
                                "> 21m gold_ore", "> 31m gold_ore", "> 32m gold_ore", "> 12m gold_ore",
                                "> 14m gold_ore", "> 28m gold_ore", "> 26m gold_ore", "> 30m gold_ore",
                                "> 29m gold_ore", "> 30m gold_ore", "> 12m gold_ore", "> 28m gold_ore",
                                "> 17m gold_ore", "> 29m gold_ore", "> 24m gold_ore", "> 25m gold_ore",
                                "> 26m gold_ore", "> 32m gold_ore",
                }, core.getPlayerMessages());
        }

        @Test
        void searchForGold_narrow() {
                final var core = new MockClientCore(new V3i(-60, -60, -51), 0f, 0f, TEST_WORLD);

                clock.timeStamp = 0x123456;
                final var sonar = narrowSonar(TEST_BLOCK_DISTANCE, Set.of(Id.ofVanilla("gold_ore")));
                sonar.ping(core);

                Iterables.assertEquals(new BlockEcho[] {
                                new BlockEcho(new V3i(-60, -60, -50), Id.ofVanilla("gold_ore"), 0x123456),
                                new BlockEcho(new V3i(-60, -60, -33), Id.ofVanilla("gold_ore"), 0x123456),
                }, sonar.echoes());

                Iterables.assertEquals(new String[] {
                                "> 1m gold_ore",
                                "> 18m gold_ore",
                }, core.getPlayerMessages());
        }

        @Test
        void checkAnotherDirectionFromMiddle_narrow() {
                final var core = new MockClientCore(V3i.ZERO, -75f, 180f, TEST_WORLD);

                clock.timeStamp = 0x123456;
                final var sonar = narrowSonar();
                sonar.ping(core);

                Iterables.assertEquals(new BlockEcho[] {
                                new BlockEcho(new V3i(0, 30, -8), Id.ofVanilla("coal_ore"), 0x123456),
                                new BlockEcho(new V3i(0, 31, -8), Id.ofVanilla("iron_ore"), 0x123456),
                }, sonar.echoes());

                Iterables.assertEquals(new String[] {
                                "> 31m coal_ore",
                                "> 32m iron_ore",
                }, core.getPlayerMessages());
        }
}
