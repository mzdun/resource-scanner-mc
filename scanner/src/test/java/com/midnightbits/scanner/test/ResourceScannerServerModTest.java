package com.midnightbits.scanner.test;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.midnightbits.scanner.ResourceScannerServerMod;
import com.midnightbits.scanner.rt.core.Services;
import com.midnightbits.scanner.rt.networking.Handshake;
import com.midnightbits.scanner.test.mocks.networking.MockPlayMessagingBridge;
import com.midnightbits.scanner.test.mocks.networking.MockServerPlayMessaging;
import com.midnightbits.scanner.test.mocks.platform.MockPlatform;

public class ResourceScannerServerModTest {
    private static class Flag {
        public boolean value = false;
    };

    private static Arguments test(String clientVersion, String serverVersion, boolean expected) {
        return Arguments.of(clientVersion, serverVersion, expected);
    }

    private static Arguments accepting(String clientVersion, String serverVersion) {
        return test(clientVersion, serverVersion, true);
    }

    private static Arguments rejecting(String clientVersion, String serverVersion) {
        return test(clientVersion, serverVersion, false);
    }

    private static Stream<Arguments> provideHandshakeTests() {
        return Stream.of(
                accepting("1.0.0", "1.0.0"),
                accepting("1.0.0", "1.15.0"),
                accepting("1.15.0", "1.0.0"),

                rejecting("2.0.0", "1.0.0"),
                rejecting(null, "1.0.0"),
                rejecting("", "1.0.0"),
                rejecting("1.0.0", null),
                rejecting("1.2-SNAPSHOT", "1.0.0"));
    }

    @ParameterizedTest
    @MethodSource("provideHandshakeTests")
    void handshake(String clientVersion, String serverVersion, boolean expected) {
        Assertions.assertInstanceOf(MockPlatform.class, Services.PLATFORM);
        final var mockPlatform = (MockPlatform) Services.PLATFORM;
        mockPlatform.setVersions(serverVersion, null);

        final var accepted = new Flag();
        final var mod = new ResourceScannerServerMod();
        final var bridge = new MockPlayMessagingBridge();
        final var playMsg = new MockServerPlayMessaging(bridge);
        mod.onInitializeServer(playMsg);

        bridge.registerClientReceiver(Handshake.REF, Handshake.class, (msg, ctx) -> {
            accepted.value = msg.version() != null;
        });

        bridge.sendMessageToServer(Handshake.of(clientVersion));

        Assertions.assertEquals(expected, accepted.value);
    }
}
