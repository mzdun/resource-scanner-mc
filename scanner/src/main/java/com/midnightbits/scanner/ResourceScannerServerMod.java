package com.midnightbits.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.midnightbits.scanner.rt.core.Services;
import com.midnightbits.scanner.rt.networking.Handshake;
import com.midnightbits.scanner.rt.networking.ServerPlayMessaging;
import com.midnightbits.scanner.rt.core.ScannerServerMod;
import com.midnightbits.scanner.utils.Manifests;
import com.midnightbits.scanner.utils.Version;

public class ResourceScannerServerMod implements ScannerServerMod {
    public static final String TAG = "resource-scanner";
    private static final Logger LOGGER = LoggerFactory.getLogger(TAG);

    @Override
    public void onInitializeServer(ServerPlayMessaging playMessaging) {
        LOGGER.warn("resource-scanner server ({} for {}, {}, {})",
                Manifests.getTagString(Services.PLATFORM.getScannerVersion()),
                Manifests.getProductVersion("MC", Services.PLATFORM.getMinecraftVersion()),
                Services.PLATFORM.getPlatformName(),
                Services.PLATFORM.getEnvironmentName());

        playMessaging.registerGlobalHandler(Handshake.REF,
                Handshake.class,
                this::onClientHandshake);
    }

    void onClientHandshake(Handshake message, ServerPlayMessaging.Context context) {
        final var version = message.version();
        final var serverVersion = Services.PLATFORM.getScannerVersion();

        var accept = false;

        if (version != null && !version.isEmpty()) {
            if (version == serverVersion) {
                accept = true;
            } else {
                final var clientVer = Version.parse(version);
                final var serverVer = Version.parse(serverVersion);
                if (clientVer != null && serverVer != null) {
                    if (clientVer.compareTo(serverVer) <= 0) {
                        accept = true;
                    } else if (clientVer.major().compareTo(serverVer.major()) == 0) {
                        accept = true;
                    }
                }
            }
        }

        if (accept) {
            LOGGER.info("Allowing resource-scanner client {}", version);
            context.responseSender().sendMessage(Handshake.accept());
        } else {
            if (version == null || version.isEmpty()) {
                LOGGER.warn("Rejecting resource-scanner client (unknown version) [server: {}]",
                        serverVersion);
            } else {
                LOGGER.warn("Rejecting resource-scanner client {} [server: {}]", version, serverVersion);
            }
            context.responseSender().sendMessage(Handshake.reject());
        }
    }
}
