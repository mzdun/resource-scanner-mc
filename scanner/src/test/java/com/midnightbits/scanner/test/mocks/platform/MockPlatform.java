package com.midnightbits.scanner.test.mocks.platform;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.midnightbits.scanner.platform.KeyBinder;
import com.midnightbits.scanner.platform.PlatformInterface;
import com.midnightbits.scanner.rt.core.ClientCore;

public class MockPlatform implements PlatformInterface, KeyBinder {
    public record KeyPressHandler(String translation, KeyBinder.KeyPressHandler handler) {
        public void handle(ClientCore client) {
            handler.handle(client);
        }
    }

    public static boolean developmentEnvironment = true;
    public Map<String, Map<Integer, KeyPressHandler>> boundKeys = new HashMap<>();

    @Override
    public String getPlatformName() {
        return "Mock";
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return developmentEnvironment;
    }

    @Override
    public boolean isDedicatedServer() {
        return false;
    }

    @Override
    public Path getGameDir() {
        try {
            return Path.of(MockPlatform.class.getClassLoader().getResource("").toURI());
        } catch (URISyntaxException e) {
            return Path.of("/");
        }
    }

    @Override
    public Path getConfigDir() {
        try {
            return Path.of(MockPlatform.class.getClassLoader().getResource("").toURI());
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
        boundKeys.computeIfAbsent(category, (key) -> new HashMap<>());
        boundKeys.get(category).put(code, new KeyPressHandler(translationKey, handler));
    }

    public MockPlatform.KeyPressHandler getHandler(int code, String category) {
        Map<Integer, KeyPressHandler> catKeys = boundKeys.get(category);
        if (catKeys == null)
            return null;
        return catKeys.get(code);
    }

    public boolean press(int code, String category, ClientCore client) {
        MockPlatform.KeyPressHandler handler = getHandler(code, category);
        if (handler == null)
            return false;

        handler.handle(client);
        return true;
    }
}
