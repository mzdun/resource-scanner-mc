// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils.test.gl;

import com.midnightbits.scanner.sonar.graphics.Colors;
import com.midnightbits.scanner.sonar.graphics.Pixel;
import org.joml.Vector3f;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;

public class FaceVerifier implements VertexVerifier {
    final Vector3f origin;
    final int side;
    final int color;

    FaceVerifier(int x, int y, int z, int side, int color) {
        this.origin = new Vector3f(x, y, z);
        this.side = side;
        this.color = color;
    }

    public int verticesNeeded() {
        return 6;
    }

    public String thisCallstackEntry() {
        return String.format("FaceVerifier(%d, %d, %d, %d, 0x%06X)", (int) origin.x, (int) origin.y, (int) origin.z,
                side, color);
    }

    public void assertTape(VertexTape tape, String stack) {
        final var pos = tape.pos();

        final List<TestVertex> expected = new ArrayList<>();
        for (final var vertex : Pixel.triangles[this.side]) {
            expected.add(VertexVerifier.apply(vertex, origin, Colors.ECHO_ALPHA | color));
        }

        final var actual = tape.nextN(6);

        Assertions.assertEquals(expected, actual, "at: " + pos + stack + "\n" + thisCallstackEntry());
    }
}
