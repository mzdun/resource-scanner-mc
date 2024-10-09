// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils.test.gl;

import com.midnightbits.scanner.sonar.graphics.Colors;
import com.midnightbits.scanner.sonar.graphics.Pixel;
import org.joml.Vector3f;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;

public class EdgeVerifier implements VertexVerifier {
    final Vector3f origin;
    final int edge;
    final int color;

    EdgeVerifier(int x, int y, int z, int edge, int color) {
        this.origin = new Vector3f(x, y, z);
        this.edge = edge;
        this.color = color;
    }

    public int verticesNeeded() {
        return 2;
    }

    public String thisCallstackEntry() {
        return String.format("EdgeVerifier(%d, %d, %d, %d, 0x%06X)", (int) origin.x, (int) origin.y, (int) origin.z,
                edge, color);
    }

    public void assertTape(VertexTape tape, String stack) {
        final var pos = tape.pos();

        final List<TestVertex> expected = new ArrayList<>();
        final var edge = Pixel.edges[this.edge];
        expected.add(VertexVerifier.apply(edge.start(), origin, Colors.OPAQUE | color));
        expected.add(VertexVerifier.apply(edge.end(), origin, Colors.OPAQUE | color));

        final var actual = tape.nextN(2);

        Assertions.assertEquals(expected, actual, "at: " + pos + diffTo("\nexpected: ", expected)
                + diffTo("\nactual: ", actual) + stack + "\n" + thisCallstackEntry());
    }

    public String diffTo(TestVertex v) {
        final var pos = v.pos();
        final int x = (int) (pos.x - origin.x);
        final int y = (int) (pos.y - origin.y);
        final int z = (int) (pos.z - origin.z);
        return "" + x + y + z;
    }

    public String diffTo(String prefix, List<TestVertex> vs) {
        final var builder = new StringBuilder();
        for (final var v : vs) {
            if (!builder.isEmpty())
                builder.append(" -> ");
            builder.append(diffTo(v));
        }
        return prefix + builder;
    }
}
