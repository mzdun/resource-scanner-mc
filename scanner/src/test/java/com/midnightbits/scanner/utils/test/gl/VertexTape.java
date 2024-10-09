// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils.test.gl;

import com.midnightbits.scanner.sonar.EchoNugget;
import com.midnightbits.scanner.sonar.graphics.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;

public class VertexTape {
    private final List<TestVertex> items;
    private int readIndex = 0;

    public VertexTape(List<TestVertex> items) {
        this.items = items;
    }

    public boolean stillHas(int vertices) {
        return vertices <= items.size() - readIndex;
    }

    public TestVertex next() {
        return items.get(readIndex++);
    }

    public List<TestVertex> nextN(int n) {
        final List<TestVertex> section = new ArrayList<>();
        while (n > 0) {
            section.add(next());
            --n;
        }
        return section;
    }

    public int pos() {
        return readIndex;
    }

    public int size() {
        return items.size();
    }

    public void assertPlayback(VertexVerifier[] template) {
        for (final var verifier : template) {
            final int pos = pos();
            final int needed = verifier.verticesNeeded();

            Assertions.assertTrue(stillHas(needed),
                    "at: " + pos + ", when asking for " + needed + " (remaining: " + (size() - pos) + ")\n"
                            + verifier.thisCallstackEntry());
            verifier.assertTape(this, "");
        }

        final int pos = pos();
        Assertions.assertFalse(stillHas(1), "at: " + pos);
    }

    public static VertexTape record(List<EchoNugget> nuggets) {
        final var context = new VerticesSink();
        final var matrices = new MatrixStack(new Matrix4f());
        final var camera = new Vector3f(-3, 0, 0);

        final var sorted = EchoNugget.sortForCamera(nuggets, camera);

        for (final var nugget : sorted) {
            nugget.draw(context, matrices, camera);
            context.items.add(TestVertex.markerWith(0x00000000));
        }

        for (final var nugget : sorted) {
            context.items.add(TestVertex.markerWith(0x00FFFFFF));
            nugget.sketch(context, matrices, camera);
        }

        return context.items();
    }
}
