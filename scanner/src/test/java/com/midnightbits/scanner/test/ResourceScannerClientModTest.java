package com.midnightbits.scanner.test;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.midnightbits.scanner.ResourceScannerClientMod;
import com.midnightbits.scanner.rt.core.Services;
import com.midnightbits.scanner.rt.core.ClientCore;
import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.core.KeyBinding;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.rt.networking.Handshake;
import com.midnightbits.scanner.rt.networking.event.ChannelIsAvailable;
import com.midnightbits.scanner.rt.networking.event.ConnectionEstablished;
import com.midnightbits.scanner.sonar.BlockEcho;
import com.midnightbits.scanner.sonar.Sonar;
import com.midnightbits.scanner.test.mocks.MockClientCore;
import com.midnightbits.scanner.test.mocks.MockWorld;
import com.midnightbits.scanner.test.mocks.MockedClock;
import com.midnightbits.scanner.test.mocks.networking.MockClientPlayMessaging;
import com.midnightbits.scanner.test.mocks.networking.MockPlayMessagingBridge;
import com.midnightbits.scanner.test.mocks.platform.MockPlatform;
import com.midnightbits.scanner.test.support.Iterables;

public class ResourceScannerClientModTest {
    private final MockedClock clock = new MockedClock();
    private static final MockWorld TEST_WORLD = MockWorld.ofResource("test_world.txt");
    private static final Logger LOGGER = LoggerFactory.getLogger("ResourceScannerClientModTest");

    @Test
    public void prepareScanner() {
        Assertions.assertInstanceOf(MockPlatform.class, Services.PLATFORM);
        final var mockPlatform = (MockPlatform) Services.PLATFORM;

        final var mod = new ResourceScannerClientMod();
        final var bridge = new MockPlayMessagingBridge();
        final var playMsg = new MockClientPlayMessaging(bridge);
        mod.onInitializeClient(playMsg);

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
    void searchForGoldWithAcceptance() {
        clock.timeStamp = 0x123456;
        final var core = new MockClientCore(new V3i(-60, -60, -51), 0f, 0f, TEST_WORLD);

        Assertions.assertInstanceOf(MockPlatform.class, Services.PLATFORM);
        final var mockPlatform = (MockPlatform) Services.PLATFORM;
        mockPlatform.setVersions("1.0.0", null);

        final var mod = new ResourceScannerClientMod();
        final var bridge = new MockPlayMessagingBridge();
        final var playMsg = new MockClientPlayMessaging(bridge);

        mod.onInitializeClient(playMsg);
        mod.setSonar(Sonar.narrow(Sonar.BLOCK_DISTANCE, Set.of(Id.ofVanilla("gold_ore"))));

        bridge.registerServerReceiver(Handshake.REF, Handshake.class, (msg, ctx) -> {
            ctx.responseSender().sendMessage(Handshake.of(msg.version()));
        });

        playMsg.dispatchEvent(new ConnectionEstablished(bridge.clientContext().responseSender()));
        playMsg.dispatchEvent(new ChannelIsAvailable(Handshake.REF.id()));

        mockPlatform.press(KeyBinding.KEY_M, KeyBinding.MOVEMENT_CATEGORY, core);

        final var expected = new BlockEcho[] {
                new BlockEcho(new V3i(-60, -60, -50), Id.ofVanilla("gold_ore"), 0x123456),
                new BlockEcho(new V3i(-60, -60, -33), Id.ofVanilla("gold_ore"), 0x123456),
        };
        Iterables.assertEquals(expected, mod.echoes());
    }

    @Test
    void searchForGoldRejectedWithNull() {
        clock.timeStamp = 0x123456;
        final var core = new MockClientCore(new V3i(-60, -60, -51), 0f, 0f, TEST_WORLD);

        Assertions.assertInstanceOf(MockPlatform.class, Services.PLATFORM);
        final var mockPlatform = (MockPlatform) Services.PLATFORM;
        mockPlatform.setVersions("1.0.0", null);

        final var mod = new ResourceScannerClientMod();
        final var bridge = new MockPlayMessagingBridge();
        final var playMsg = new MockClientPlayMessaging(bridge);

        mod.onInitializeClient(playMsg);
        mod.setSonar(Sonar.narrow(Sonar.BLOCK_DISTANCE, Set.of(Id.ofVanilla("gold_ore"))));

        bridge.registerServerReceiver(Handshake.REF, Handshake.class, (msg, ctx) -> {
            ctx.responseSender().sendMessage(Handshake.of(null));
        });

        playMsg.dispatchEvent(new ConnectionEstablished(bridge.clientContext().responseSender()));
        playMsg.dispatchEvent(new ChannelIsAvailable(Id.ofModuleV1("unused-message")));
        playMsg.dispatchEvent(new ChannelIsAvailable(Handshake.REF.id()));

        mockPlatform.press(KeyBinding.KEY_M, KeyBinding.MOVEMENT_CATEGORY, core);

        final var expected = new BlockEcho[] {};
        Iterables.assertEquals(expected, mod.echoes());
    }

    @Test
    void searchForGoldRejectedWithEmpty() {
        clock.timeStamp = 0x123456;
        final var core = new MockClientCore(new V3i(-60, -60, -51), 0f, 0f, TEST_WORLD);

        Assertions.assertInstanceOf(MockPlatform.class, Services.PLATFORM);
        final var mockPlatform = (MockPlatform) Services.PLATFORM;
        mockPlatform.setVersions("1.0.0", null);

        final var mod = new ResourceScannerClientMod();
        final var bridge = new MockPlayMessagingBridge();
        final var playMsg = new MockClientPlayMessaging(bridge);

        mod.onInitializeClient(playMsg);
        mod.setSonar(Sonar.narrow(Sonar.BLOCK_DISTANCE, Set.of(Id.ofVanilla("gold_ore"))));

        bridge.registerServerReceiver(Handshake.REF, Handshake.class, (msg, ctx) -> {
            ctx.responseSender().sendMessage(Handshake.of(""));
        });

        playMsg.dispatchEvent(new ConnectionEstablished(bridge.clientContext().responseSender()));
        playMsg.dispatchEvent(new ChannelIsAvailable(Id.ofModuleV1("unused-message")));
        playMsg.dispatchEvent(new ChannelIsAvailable(Handshake.REF.id()));

        mockPlatform.press(KeyBinding.KEY_M, KeyBinding.MOVEMENT_CATEGORY, core);

        final var expected = new BlockEcho[] {};
        Iterables.assertEquals(expected, mod.echoes());
    }

    @Test
    void searchForGoldWithoutAgreement() {
        clock.timeStamp = 0x123456;
        final var core = new MockClientCore(new V3i(-60, -60, -51), 0f, 0f, TEST_WORLD);
        runScannerWith(core, Sonar.narrow(Sonar.BLOCK_DISTANCE, Set.of(Id.ofVanilla("gold_ore"))), true,
                new BlockEcho[] {});
    }

    @Test
    void lookUp() {
        final var core = new MockClientCore(V3i.ZERO, 0f, 0f, TEST_WORLD);
        runScannerWith(core, new BlockEcho[] {});
    }

    void runScannerWith(ClientCore core, BlockEcho[] expected) {
        runScannerWith(core, null, false, expected);
    }

    void runScannerWith(ClientCore core, Sonar sonar, BlockEcho[] expected) {
        runScannerWith(core, sonar, false, expected);
    }

    void runScannerWith(ClientCore core, Sonar sonar, boolean useFairPlay, BlockEcho[] expected) {
        Assertions.assertInstanceOf(MockPlatform.class, Services.PLATFORM);
        final var mockPlatform = (MockPlatform) Services.PLATFORM;

        final var mod = new ResourceScannerClientMod();
        final var bridge = new MockPlayMessagingBridge();
        final var playMsg = new MockClientPlayMessaging(bridge);
        mod.onInitializeClient(playMsg);
        mod.setFairPlay(useFairPlay);

        if (sonar != null) {
            mod.setSonar(sonar);
        }

        mockPlatform.press(KeyBinding.KEY_M, KeyBinding.MOVEMENT_CATEGORY, core);
        Iterables.assertEquals(expected, mod.echoes());
    }
}
