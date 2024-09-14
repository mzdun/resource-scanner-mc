package com.midnightbits.scanner.platform;

import java.nio.file.Path;

public interface PlatformInterface {
    String getPlatformName();

    boolean isDevelopmentEnvironment();

    default String getEnvironmentName() {
        return this.isDevelopmentEnvironment() ? "development" : "production";
    }

    boolean isDedicatedServer();

    Path getGameDir();

    Path getConfigDir();

    KeyBinder getKeyBinder();
}
