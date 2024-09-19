package com.midnightbits.scanner.rt.core;

import com.midnightbits.scanner.sonar.BlockEcho;

public interface ScannerMod {
    void onInitializeClient();

    Iterable<BlockEcho> echoes();
}
