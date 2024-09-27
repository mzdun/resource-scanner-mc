// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.rt.core;

import com.midnightbits.scanner.sonar.BlockEcho;

public interface ScannerMod {
    void onInitializeClient();

    Iterable<BlockEcho> echoes();
}
