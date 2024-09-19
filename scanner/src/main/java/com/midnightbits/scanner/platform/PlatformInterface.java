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
