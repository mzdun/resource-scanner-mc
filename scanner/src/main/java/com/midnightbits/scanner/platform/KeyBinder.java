package com.midnightbits.scanner.platform;

import com.midnightbits.scanner.rt.core.ClientCore;

public interface KeyBinder {
    @FunctionalInterface
    public static interface KeyPressHandler {
        void handle(ClientCore client);
    }

    void bind(String translationKey, int code, String category, KeyPressHandler handler);
}
