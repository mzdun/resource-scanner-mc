// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.platform;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.sonar.graphics.AbstractAnimatorHost;
import com.midnightbits.scanner.sonar.graphics.ColorDefaults;

import java.nio.file.Path;
import java.util.Map;

public interface PlatformInterface {
    enum Sample {
        ACTIVATED,
    };

    String getPlatformName();

    String getScannerVersion();

    String getMinecraftVersion();

    boolean isDevelopmentEnvironment();

    default String getEnvironmentName() {
        return this.isDevelopmentEnvironment() ? "development" : "production";
    }

    Path getConfigDir();

    KeyBinder getKeyBinder();

    AbstractAnimatorHost getAnimatorHost();

    void playSample(Sample id);

    default Map<Id, Integer> getBlockTagColors() {
        return ColorDefaults.BLOCK_TAG_COLORS;
    }
}
