// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.graphics;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.sonar.Echo;
import com.midnightbits.scanner.sonar.EchoState;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class Shimmers {
    private final static Colors.Proxy SHIMMER_BLUE = new Colors.DirectValue(0x8080FF);
    private final static Echo shimmer = new Echo(Id.ofMod("shimmer-echo"), SHIMMER_BLUE);

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

    public Stream<EchoState> toEchoStates(double alphaMax) {
        final var alphaChannel = (int) Math.round(255 * (this.alpha * alphaMax));
        final var alpha = alphaChannel << 24;
        if (alpha == 0) {
            return Stream.of();
        }

        return blocks.stream()
                .map((pos) -> new EchoState(pos, shimmer, 0))
                .peek((state) -> state.alpha = alpha);
    }

    public static Stream<EchoState> toEchoStates(List<Shimmers> waves, double alphaMax) {
        return waves.stream()
                .flatMap(wave -> wave.toEchoStates(alphaMax));
    }
}
