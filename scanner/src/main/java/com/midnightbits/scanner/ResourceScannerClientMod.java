package com.midnightbits.scanner;

import java.nio.file.Path;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.midnightbits.scanner.rt.core.Services;
import com.midnightbits.scanner.rt.networking.ClientPlayMessaging;
import com.midnightbits.scanner.rt.networking.Handshake;
import com.midnightbits.scanner.rt.networking.event.ChannelIsAvailable;
import com.midnightbits.scanner.rt.networking.event.ConnectionEstablished;
import com.midnightbits.scanner.rt.core.ClientCore;
import com.midnightbits.scanner.rt.core.KeyBinding;
import com.midnightbits.scanner.rt.core.ScannerClientMod;
import com.midnightbits.scanner.sonar.BlockEcho;
import com.midnightbits.scanner.sonar.BlockEchoes;
import com.midnightbits.scanner.sonar.Sonar;
import com.midnightbits.scanner.utils.ConfigFile;
import com.midnightbits.scanner.utils.Manifests;

public class ResourceScannerClientMod implements ScannerClientMod {
    public static final String TAG = "resource-scanner";
    private static final Logger LOGGER = LoggerFactory.getLogger(TAG);

    private final ConfigFile config = new ConfigFile();
    private Sonar sonar = new Sonar();
    private boolean useFairPlay = true;
    private boolean fairPlay = false;

    @Override
    public void onInitializeClient(ClientPlayMessaging playMessaging) {
        config.addEventListener((event) -> {
            var settings = event.settings();
            useFairPlay = settings.fairPlay();
            this.sonar.refresh(
                    settings.blockDistance(), settings.blockRadius(), settings.interestingIds(), settings.echoesSize());
        });

        playMessaging.addEventListener(ChannelIsAvailable.class, (event) -> {
            if (event.id().equals(Handshake.REF.id())) {
                playMessaging.send(Handshake.ofScannerVersion());
            }
        });

        playMessaging.addEventListener(ConnectionEstablished.class, (event) -> {
            fairPlay = false;
            LOGGER.info("Disabling resource-scanner client until server accepts it");
        });

        playMessaging.registerGlobalHandler(Handshake.REF,
                Handshake.class,
                (message, context) -> {
                    final var version = message.version();
                    if (version != null && !version.isEmpty()) {
                        fairPlay = true;
                        LOGGER.info("The resource-scanner client is now enabled");
                    }
                });

        LOGGER.warn("resource-scanner client ({} for {}, {}, {})",
                Manifests.getTagString(Services.PLATFORM.getScannerVersion()),
                Manifests.getProductVersion("MC", Services.PLATFORM.getMinecraftVersion()),
                Services.PLATFORM.getPlatformName(),
                Services.PLATFORM.getEnvironmentName());
        Path configDir = Services.PLATFORM.getConfigDir();
        LOGGER.debug("conf dir: {}", configDir);

        config.setDirectory(configDir);
        if (!config.load()) {
            config.setAll(
                    BlockEchoes.MAX_SIZE,
                    Sonar.BLOCK_DISTANCE,
                    Sonar.BLOCK_RADIUS,
                    Set.of(Sonar.INTERESTING_IDS),
                    true,
                    false);
        }

        Services.PLATFORM.getKeyBinder().bind(
                "key.resource-scanner.scan",
                KeyBinding.KEY_M,
                KeyBinding.MOVEMENT_CATEGORY,
                this::onScanPressed);
    }

    private void onScanPressed(ClientCore client) {
        if (useFairPlay && !fairPlay) {
            LOGGER.warn("This mod must be present on server to indicate clients may use the sonar");
            return;
        }

        if (!sonar.ping(client))
            return;

        for (BlockEcho echo : sonar.echoes()) {
            LOGGER.info("{} ({}) {}", echo.getPingTime(), echo.getPosition(), echo.getId());
        }
        LOGGER.info("");
    }

    @Override
    public Iterable<BlockEcho> echoes() {
        return sonar.echoes();
    }

    public void setSonar(Sonar sonar) {
        this.sonar = sonar;
    }

    public void setFairPlay(boolean useFairPlay) {
        this.useFairPlay = useFairPlay;
    }
}
