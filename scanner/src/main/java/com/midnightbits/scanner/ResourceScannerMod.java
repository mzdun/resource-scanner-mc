// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner;

import java.nio.file.Path;
import java.util.Set;

import com.midnightbits.scanner.sonar.EchoState;
import com.midnightbits.scanner.sonar.graphics.SonarAnimation;

import com.midnightbits.scanner.utils.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.midnightbits.scanner.rt.core.Services;
import com.midnightbits.scanner.platform.PlatformInterface;
import com.midnightbits.scanner.rt.core.ClientCore;
import com.midnightbits.scanner.rt.core.KeyBindings;
import com.midnightbits.scanner.rt.core.ScannerMod;
import com.midnightbits.scanner.sonar.Echoes;
import com.midnightbits.scanner.sonar.Sonar;
import com.midnightbits.scanner.utils.Options;
import com.midnightbits.scanner.utils.Manifests;

public class ResourceScannerMod implements ScannerMod {
    public static final Logger LOGGER = LoggerFactory.getLogger(ScannerMod.MOD_ID);

    private final Sonar sonar;
    private final SonarAnimation animation;

    public ResourceScannerMod(Sonar sonar) {
        this.sonar = sonar;
        this.animation = new SonarAnimation(sonar);
    }

    public ResourceScannerMod() {
        this(new Sonar());
    }

    @Override
    public void onInitializeClient() {
        LOGGER.warn("{} ({} for {}, {}, {})",
                ScannerMod.MOD_ID,
                Manifests.getTagString(Services.PLATFORM.getScannerVersion()),
                Manifests.getProductVersion("MC", Services.PLATFORM.getMinecraftVersion()),
                Services.PLATFORM.getPlatformName(),
                Services.PLATFORM.getEnvironmentName());
        Path configDir = Services.PLATFORM.getConfigDir();
        LOGGER.debug("conf dir: {}", configDir);

        final var options = Options.getInstance();
        options.addEventListener((event) -> {
            refresh(event.settings());
        });

        options.setDirectory(configDir);
        if (!options.load()) {
            options.setAll(
                    Sonar.BLOCK_DISTANCE,
                    Sonar.BLOCK_RADIUS,
                    Echoes.ECHO_LIFETIME,
                    Set.of(Sonar.INTERESTING_IDS),
                    false);
        }

        Services.PLATFORM.getKeyBinder().bind(
                ScannerMod.translationKey("key", "scan"),
                KeyBindings.SCAN_BUTTON,
                KeyBindings.GAMEPLAY_CATEGORY,
                this::onScanPressed);
    }

    public void refresh(Settings settings) {
        this.sonar.refresh(settings);
    }

    private void onScanPressed(ClientCore client) {
        if (animation.sendPing(client, this::listEchoes)) {
            Services.PLATFORM.playSample(PlatformInterface.Sample.ACTIVATED);
        }
    }

    @Override
    public Iterable<EchoState> echoes() {
        return sonar.echoes();
    }

    @Override
    public Sonar getSonar() {
        return sonar;
    }

    private void listEchoes() {
        for (final var echo : sonar.echoes()) {
            LOGGER.info("{} ({}) {}", echo.pingTime(), echo.position(), echo.id());
        }
        LOGGER.info("");
    }
}
