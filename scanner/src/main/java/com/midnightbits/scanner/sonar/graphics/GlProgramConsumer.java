// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.graphics;

import org.joml.Matrix4f;

public interface GlProgramConsumer {
    void vertexColor(Matrix4f matrix, float x, float y, float z, int argb32);
}
