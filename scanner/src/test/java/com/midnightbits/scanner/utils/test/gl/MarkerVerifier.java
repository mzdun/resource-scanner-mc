// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils.test.gl;

import org.junit.jupiter.api.Assertions;

public class MarkerVerifier implements VertexVerifier {
    final int color;

    public MarkerVerifier(int color) {
        this.color = color;
    }

    public int verticesNeeded() {
        return 1;
    }

    public String thisCallstackEntry() {
        return String.format("MarkerVerifier(0x%06X)", color);
    }

    public void assertTape(VertexTape tape, String stack) {
        final var pos = tape.pos();
        final var marker = tape.next();
        Assertions.assertEquals(TestVertex.markerWith(color), marker,
                "at: " + pos + stack + "\n" + thisCallstackEntry());
    }
}
