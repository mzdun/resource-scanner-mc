// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.platform;

import java.nio.file.Path;

public interface PlatformInterface {
    String getPlatformName();

    String getScannerVersion();

    String getMinecraftVersion();

    boolean isDevelopmentEnvironment();

    default String getEnvironmentName() {
        return this.isDevelopmentEnvironment() ? "development" : "production";
    }

    Path getConfigDir();

    KeyBinder getKeyBinder();
}
