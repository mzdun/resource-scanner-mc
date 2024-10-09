// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils.test.gl;

import org.joml.Vector3f;

public record TestVertex(Vector3f pos, int argb32) {
    static TestVertex markerWith(int argb32) {
        return new TestVertex(new Vector3f(0, 0, 0), argb32);
    }

    public String toString() {
        return "\nTestVertex.of(" + pos.x + "F, " + pos.y + "F, " + pos.z + "F, " + String.format("0x%08X", argb32)
                + ")";
    }
}
