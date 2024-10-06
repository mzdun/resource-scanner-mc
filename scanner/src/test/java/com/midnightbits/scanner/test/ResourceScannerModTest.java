// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.test;

import java.util.Set;

import com.midnightbits.scanner.sonar.graphics.SlicePacer;
import com.midnightbits.scanner.sonar.graphics.WaveAnimator;
import com.midnightbits.scanner.sonar.test.SonarTest;
import com.midnightbits.scanner.test.mocks.platform.MockAnimatorHost;
import com.midnightbits.scanner.utils.Settings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.ResourceScannerMod;
import com.midnightbits.scanner.rt.core.Services;
import com.midnightbits.scanner.rt.core.ClientCore;
import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.core.KeyBinding;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.sonar.BlockEcho;
import com.midnightbits.scanner.test.mocks.MockClientCore;
import com.midnightbits.scanner.test.mocks.MockWorld;
import com.midnightbits.scanner.test.mocks.MockedClock;
import com.midnightbits.scanner.test.mocks.platform.MockPlatform;
import com.midnightbits.scanner.test.support.Iterables;

public class ResourceScannerModTest {
    private final MockedClock clock = new MockedClock();
    private static final MockWorld TEST_WORLD = MockWorld.ofResource("test_world.txt");

    @Test
    public void prepareScanner() {
        Assertions.assertInstanceOf(MockPlatform.class, Services.PLATFORM);
        final var mockPlatform = (MockPlatform) Services.PLATFORM;

        final var mod = new ResourceScannerMod();
        mod.onInitializeClient();

        Assertions.assertNotNull(mockPlatform.getHandler(KeyBinding.KEY_M, KeyBinding.MOVEMENT_CATEGORY));
    }

    @Test
    void checkDownwardsDirectionFromMiddle() {
        clock.timeStamp = 0x123456;
        final var core = new MockClientCore(V3i.ZERO, -90, 0, TEST_WORLD);
        final var offset = 0x123456 + WaveAnimator.DURATION + SlicePacer.DURATION;
        runScannerWith(core, SonarTest.narrowSonar(), new BlockEcho[] {
                new BlockEcho(new V3i(0, 23, 0), Id.ofVanilla("deepslate_iron_ore"), offset + 23 * SlicePacer.DURATION),
                new BlockEcho(new V3i(0, 25, 0), Id.ofVanilla("deepslate_diamond_ore"), offset + 25 * SlicePacer.DURATION),
                new BlockEcho(new V3i(0, 27, 0), Id.ofVanilla("diamond_ore"), offset + 27 * SlicePacer.DURATION),
                new BlockEcho(new V3i(0, 28, 0), Id.ofVanilla("iron_ore"), offset + 28 * SlicePacer.DURATION),
                new BlockEcho(new V3i(0, 30, 0), Id.ofVanilla("iron_ore"), offset + 30 * SlicePacer.DURATION),
        });
    }

    @Test
    void searchForGold() {
        clock.timeStamp = 0x123456;
        final var core = new MockClientCore(new V3i(-60, -60, -51), 0f, 0f, TEST_WORLD);
        final var offset = 0x123456 + WaveAnimator.DURATION + SlicePacer.DURATION;
        runScannerWith(core, SonarTest.narrowSonar(SonarTest.TEST_BLOCK_DISTANCE, Set.of(Id.ofVanilla("gold_ore"))),
                new BlockEcho[] {
                        new BlockEcho(new V3i(-60, -60, -50), Id.ofVanilla("gold_ore"), offset + SlicePacer.DURATION),
                        new BlockEcho(new V3i(-60, -60, -33), Id.ofVanilla("gold_ore"), offset + 18 * SlicePacer.DURATION),
                });
    }

    @Test
    void lookUp() {
        final var core = new MockClientCore(V3i.ZERO, 0f, 0f, TEST_WORLD);
        runScannerWith(core, new BlockEcho[] {});
    }

    void runScannerWith(ClientCore core, BlockEcho[] expected) {
        runScannerWith(core, null, expected);
    }

    void runScannerWith(ClientCore core, Settings settings, BlockEcho[] expected) {
        Assertions.assertInstanceOf(MockPlatform.class, Services.PLATFORM);
        final var mockPlatform = (MockPlatform) Services.PLATFORM;
        final var mockAnimatorHost = (MockAnimatorHost) mockPlatform.getAnimatorHost();

        final var mod = new ResourceScannerMod();
        mod.onInitializeClient();

        if (settings != null) {
            mod.refresh(settings);
        }

        mockPlatform.press(KeyBinding.KEY_M, KeyBinding.MOVEMENT_CATEGORY, core);
        mockAnimatorHost.tickWith(clock);
        mockAnimatorHost.tickWith(clock);
        mockAnimatorHost.tickWith(clock);
        mockPlatform.press(KeyBinding.KEY_M, KeyBinding.MOVEMENT_CATEGORY, core);
        mockAnimatorHost.runAll(clock);
        Iterables.assertEquals(expected, mod.echoes());
    }
}
