package com.midnightbits.scanner.rt.core;

import com.midnightbits.scanner.sonar.BlockEcho;
import com.midnightbits.scanner.sonar.Sonar;

public interface ScannerMod {
    public void onInitializeClient();

    public void setSonar(Sonar sonar);

    public Iterable<BlockEcho> echoes();
}
