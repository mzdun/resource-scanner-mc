// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils.test.gl;

import com.midnightbits.scanner.rt.core.ScannerMod;
import com.midnightbits.scanner.sonar.graphics.Pixel;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface VertexVerifier {
    String TAG = ScannerMod.MOD_ID;
    Logger LOGGER = LoggerFactory.getLogger(TAG);

    int verticesNeeded();

    void assertTape(VertexTape tape, String stack);

    String thisCallstackEntry();

    static TestVertex apply(Pixel.Vertex v, Vector3f origin, int color) {
        return new TestVertex(origin.add(v.x(), v.y(), v.z(), new Vector3f()), color);
    }
}