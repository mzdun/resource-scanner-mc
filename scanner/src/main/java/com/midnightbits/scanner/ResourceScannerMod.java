package com.midnightbits.scanner;

import java.nio.file.Path;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.midnightbits.scanner.rt.core.Services;
import com.midnightbits.scanner.rt.core.ClientCore;
import com.midnightbits.scanner.rt.core.KeyBinding;
import com.midnightbits.scanner.rt.core.ScannerMod;
import com.midnightbits.scanner.sonar.BlockEcho;
import com.midnightbits.scanner.sonar.BlockEchoes;
import com.midnightbits.scanner.sonar.Sonar;
import com.midnightbits.scanner.utils.ConfigFile;
import com.midnightbits.scanner.utils.Manifests;

public class ResourceScannerMod implements ScannerMod {
    public static final String TAG = "resource-scanner";
    private static final Logger LOGGER = LoggerFactory.getLogger(TAG);

    private final ConfigFile config = new ConfigFile();
    private Sonar sonar = new Sonar();

    @Override
    public void onInitializeClient() {
        config.addEventListener((event) -> {
            var settings = event.settings();
            this.sonar.refresh(
                    settings.blockDistance(), settings.blockRadius(), settings.interestingIds(), settings.echoesSize());
        });

        LOGGER.warn("resource-scanner ({} for {}, {}, {})",
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
                    false);
        }

        Services.PLATFORM.getKeyBinder().bind(
                "key.resource-scanner.scan",
                KeyBinding.KEY_M,
                KeyBinding.MOVEMENT_CATEGORY,
                this::onScanPressed);
    }

    private void onScanPressed(ClientCore client) {
        if (!sonar.ping(client))
            return;

        for (BlockEcho echo : sonar.echoes()) {
            LOGGER.info("{} ({}) {}", echo.pingTime(), echo.position(), echo.id());
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
}
