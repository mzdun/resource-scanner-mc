// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.test.mocks.platform;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.midnightbits.scanner.platform.KeyBinder;
import com.midnightbits.scanner.platform.PlatformInterface;
import com.midnightbits.scanner.rt.core.ClientCore;
import com.midnightbits.scanner.sonar.BlockEcho;
import com.midnightbits.scanner.sonar.graphics.AbstractAnimatorHost;
import com.midnightbits.scanner.sonar.graphics.GraphicContext;
import com.midnightbits.scanner.sonar.graphics.Shimmers;

public final class MockPlatform implements PlatformInterface, KeyBinder {
    public record KeyPressHandler(String translation, KeyBinder.KeyPressHandler handler) {
        public void handle(ClientCore client) {
            handler.handle(client);
        }
    }

    public static boolean developmentEnvironment = true;
    public Map<String, Map<Integer, KeyPressHandler>> boundKeys = new HashMap<>();
    private String scannerVersion = null;
    private String minecraftVersion = null;
    private MockAnimatorHost host = null;

    public MockPlatform() {
        setDefaultHostBackend();
    }

    @Override
    public String getPlatformName() {
        return "Mock";
    }

    @Override
    public String getScannerVersion() {
        return scannerVersion;
    }

    @Override
    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return developmentEnvironment;
    }

    @Override
    public Path getConfigDir() {
        try {
            return Path.of(Objects.requireNonNull(MockPlatform.class.getClassLoader().getResource("")).toURI());
        } catch (URISyntaxException e) {
            return Path.of("/");
        }
    }

    @Override
    public KeyBinder getKeyBinder() {
        return this;
    }

    @Override
    public AbstractAnimatorHost getAnimatorHost() {
        if (host == null) {
            throw new NullPointerException();
        }
        return host;
    }

    @Override
    public void bind(String translationKey, int code, String category, KeyBinder.KeyPressHandler handler) {
        boundKeys.computeIfAbsent(category, (key) -> new HashMap<>());
        boundKeys.get(category).put(code, new KeyPressHandler(translationKey, handler));
    }

    public interface ScanDrawer {
        void drawScan(Iterable<BlockEcho> echoes, List<Shimmers> shimmers);
    }

    public void setHostBackend(GraphicContext painter) {
        host = new MockAnimatorHost(() -> painter);
    }

    public void setDefaultHostBackend() {
        host = new MockAnimatorHost(() -> (echoes, shimmers) -> {
        });
    }

    public void setVersions(String scannerVersion, String minecraftVersion) {
        this.scannerVersion = scannerVersion;
        this.minecraftVersion = minecraftVersion;
    }

    public MockPlatform.KeyPressHandler getHandler(int code, String category) {
        Map<Integer, KeyPressHandler> catKeys = boundKeys.get(category);
        if (catKeys == null)
            return null;
        return catKeys.get(code);
    }

    public void press(int code, String category, ClientCore client) {
        MockPlatform.KeyPressHandler handler = getHandler(code, category);
        if (handler == null)
            return;

        handler.handle(client);
    }
}
