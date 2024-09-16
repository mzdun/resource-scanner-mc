package com.midnightbits.scanner.fabric;

import java.nio.file.Path;

import com.midnightbits.scanner.platform.KeyBinder;
import com.midnightbits.scanner.platform.PlatformInterface;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class FabricPlatform implements PlatformInterface {
    private final FabricKeyBinder binder = new FabricKeyBinder();

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public boolean isDedicatedServer() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    @Override
    public Path getGameDir() {
        return FabricLoader.getInstance().getGameDir().normalize();
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public KeyBinder getKeyBinder() {
        return binder;
    }
}
