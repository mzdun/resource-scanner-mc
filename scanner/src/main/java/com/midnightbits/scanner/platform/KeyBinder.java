// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.platform;

import com.midnightbits.scanner.rt.core.ClientCore;
import com.midnightbits.scanner.rt.core.KeyBindings;

public interface KeyBinder {
    @FunctionalInterface
    interface KeyPressHandler {
        void handle(ClientCore client);
    }

    void bind(String translationKey, int glfwCode, String category, KeyPressHandler handler);
    void bind(String translationKey, KeyBindings.MOUSE code, String category, KeyPressHandler handler);
}
