// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils.test.gl;

import com.midnightbits.scanner.sonar.graphics.GlProgramConsumer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class VerticesSink implements GlProgramConsumer {
    final List<TestVertex> items = new ArrayList<>();

    @Override
    public void vertexColor(Matrix4f matrix, float x, float y, float z, int argb32) {
        final var pos = matrix.transformPosition(x, y, z, new Vector3f());
        items.add(new TestVertex(pos, argb32));
    }

    VertexTape items() {
        return new VertexTape(items);
    }
}
