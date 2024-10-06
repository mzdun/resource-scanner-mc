// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar;

import com.midnightbits.scanner.rt.math.V3i;

import java.util.List;

public interface ScanWaveConsumer {
    void advance(List<V3i> shimmers, List<BlockEcho.Partial> echoes);
}
