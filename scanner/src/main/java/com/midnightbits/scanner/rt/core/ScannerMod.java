// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.rt.core;

import com.midnightbits.scanner.sonar.EchoState;
import com.midnightbits.scanner.sonar.Sonar;

public interface ScannerMod {
    String MOD_ID = "resource-scanner";

    static String translationKey(String category, String id) {
        return category + "." + MOD_ID + "." + id;
    }

    void onInitializeClient();

    Iterable<EchoState> echoes();

    Sonar getSonar();
}
