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
import com.midnightbits.scanner.rt.core.KeyBindings;
import com.midnightbits.scanner.sonar.EchoState;
import com.midnightbits.scanner.sonar.graphics.AbstractAnimatorHost;
import com.midnightbits.scanner.sonar.graphics.ShimmerConsumer;
import com.midnightbits.scanner.sonar.graphics.Shimmers;

public final class MockPlatform implements PlatformInterface, KeyBinder {
    public record KeyPressHandler(String translation, KeyBinder.KeyPressHandler handler) {
        public void handle(ClientCore client) {
            handler.handle(client);
        }
    }

    public enum KeyType {
        KEYSYM,
        MOUSE
    };

    public static boolean developmentEnvironment = true;
    public Map<KeyType, Map<Integer, KeyPressHandler>> boundKeys = new HashMap<>();
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
    public void bind(String translationKey, int code, String category, KeyBinder.KeyPressHandler handler) {
        boundKeys.computeIfAbsent(KeyType.KEYSYM, (key) -> new HashMap<>());
        boundKeys.get(KeyType.KEYSYM).put(code, new KeyPressHandler(translationKey, handler));
    }

    @Override
    public void bind(String translationKey, KeyBindings.MOUSE code, String category, KeyBinder.KeyPressHandler handler) {
        boundKeys.computeIfAbsent(KeyType.MOUSE, (key) -> new HashMap<>());
        boundKeys.get(KeyType.MOUSE).put(code.button(), new KeyPressHandler(translationKey, handler));
    }

    @Override
    public AbstractAnimatorHost getAnimatorHost() {
        if (host == null) {
            throw new NullPointerException();
        }
        return host;
    }

    @Override
    public void playSample(Sample id) {
    }

    public interface ScanDrawer {
        void drawScan(Iterable<EchoState> echoes, List<Shimmers> shimmers);
    }

    public void setHostBackend(ShimmerConsumer painter) {
        host = new MockAnimatorHost(() -> painter);
    }

    public void setDefaultHostBackend() {
        host = new MockAnimatorHost(() -> (shimmers) -> {
        });
    }

    public void setVersions(String scannerVersion, String minecraftVersion) {
        this.scannerVersion = scannerVersion;
        this.minecraftVersion = minecraftVersion;
    }

    public MockPlatform.KeyPressHandler getHandler(int code, KeyType type) {
        Map<Integer, KeyPressHandler> catKeys = boundKeys.get(type);
        if (catKeys == null)
            return null;
        return catKeys.get(code);
    }

    public MockPlatform.KeyPressHandler getHandler(KeyBindings.MOUSE code) {
        return getHandler(code.button(), KeyType.MOUSE);
    }

    public void press(int code, KeyType type, ClientCore client) {
        MockPlatform.KeyPressHandler handler = getHandler(code, type);
        if (handler == null)
            return;

        handler.handle(client);
    }

    public void press(KeyBindings.MOUSE code, ClientCore client) {
        press(code.button(), KeyType.MOUSE, client);
    }
}
