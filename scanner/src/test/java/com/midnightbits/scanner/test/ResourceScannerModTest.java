package com.midnightbits.scanner.test;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.ResourceScannerMod;
import com.midnightbits.scanner.rt.core.Services;
import com.midnightbits.scanner.rt.core.ClientCore;
import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.core.KeyBinding;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.sonar.BlockEcho;
import com.midnightbits.scanner.sonar.Sonar;
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
        runScannerWith(core, Sonar.narrow(), new BlockEcho[] {
                new BlockEcho(new V3i(0, 25, 0), Id.ofVanilla("deepslate_diamond_ore"), 0x123456),
                new BlockEcho(new V3i(0, 23, 0), Id.ofVanilla("deepslate_iron_ore"), 0x123456),
                new BlockEcho(new V3i(0, 27, 0), Id.ofVanilla("diamond_ore"), 0x123456),
                new BlockEcho(new V3i(0, 28, 0), Id.ofVanilla("iron_ore"), 0x123456),
                new BlockEcho(new V3i(0, 30, 0), Id.ofVanilla("iron_ore"), 0x123456),
        });
    }

    @Test
    void searchForGold() {
        clock.timeStamp = 0x123456;
        final var core = new MockClientCore(new V3i(-60, -60, -51), 0f, 0f, TEST_WORLD);
        runScannerWith(core, Sonar.narrow(Sonar.BLOCK_DISTANCE, Set.of(Id.ofVanilla("gold_ore"))), new BlockEcho[] {
                new BlockEcho(new V3i(-60, -60, -50), Id.ofVanilla("gold_ore"), 0x123456),
                new BlockEcho(new V3i(-60, -60, -33), Id.ofVanilla("gold_ore"), 0x123456),
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

    void runScannerWith(ClientCore core, Sonar sonar, BlockEcho[] expected) {
        Assertions.assertInstanceOf(MockPlatform.class, Services.PLATFORM);
        final var mockPlatform = (MockPlatform) Services.PLATFORM;

        final var mod = new ResourceScannerMod();
        mod.onInitializeClient();

        if (sonar != null) {
            mod.setSonar(sonar);
        }

        mockPlatform.press(KeyBinding.KEY_M, KeyBinding.MOVEMENT_CATEGORY, core);
        Iterables.assertEquals(expected, mod.echoes());
    }
}
