package com.midnightbits.scanner.rt.core;

import com.midnightbits.scanner.sonar.BlockEcho;
import com.midnightbits.scanner.sonar.Sonar;

public interface ScannerMod {
    void onInitializeClient();

    void setSonar(Sonar sonar);

    Iterable<BlockEcho> echoes();
}
