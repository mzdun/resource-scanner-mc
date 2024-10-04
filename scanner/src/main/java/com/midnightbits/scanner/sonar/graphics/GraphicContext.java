package com.midnightbits.scanner.sonar.graphics;

import com.midnightbits.scanner.sonar.BlockEcho;

import java.util.List;

public interface GraphicContext {
    void drawScan(Iterable<BlockEcho> echoes, List<Shimmers> shimmers);
}
