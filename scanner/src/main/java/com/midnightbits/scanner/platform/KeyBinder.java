// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.platform;

import com.midnightbits.scanner.rt.core.ClientCore;

public interface KeyBinder {
    @FunctionalInterface
    interface KeyPressHandler {
        void handle(ClientCore client);
    }

    void bind(String translationKey, int code, String category, KeyPressHandler handler);
}
