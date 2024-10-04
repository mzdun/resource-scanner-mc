package com.midnightbits.scanner.sonar;

import com.midnightbits.scanner.rt.math.V3i;

import java.util.List;

public interface ScanWaveConsumer {
    void advance(List<V3i> shimmers, List<BlockEcho.Partial> echoes);
}
