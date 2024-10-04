package com.midnightbits.scanner.sonar.graphics;

import com.midnightbits.scanner.rt.math.V3i;

import java.util.List;

public class Shimmers {
    private final List<V3i> blocks;
    private double alpha = 0;

    public Shimmers(List<V3i> blocks) {
        this.blocks = blocks;
    }

    public List<V3i> blocks() {
        return blocks;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double alpha() {
        return alpha;
    }
}
