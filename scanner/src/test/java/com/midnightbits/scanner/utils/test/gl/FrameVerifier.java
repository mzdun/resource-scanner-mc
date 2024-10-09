// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils.test.gl;

import com.midnightbits.scanner.sonar.graphics.Pixel;

import java.util.ArrayList;
import java.util.List;

public class FrameVerifier extends SubVerifier {
    public FrameVerifier(int x, int y, int z, int edges, int color) {
        super(makeEdges(x, y, z, edges, color),
                String.format("FrameVerifier(%d, %d, %d, 0x%x, 0x%X)", x, y, z, edges, color));
    }

    private static List<VertexVerifier> makeEdges(int x, int y, int z, int edges, int color) {
        List<VertexVerifier> result = new ArrayList<>();
        for (int i = 0; i < Pixel.edges.length; ++i) {
            final int flag = 1 << i;
            if ((edges & flag) == 0) {
                VertexVerifier.LOGGER.debug(String.format(
                        "FrameVerifier(%d, %d, %d, 0x%x, 0x%X): skipping: %s/%s",
                        x, y, z, edges, color, i, Pixel.edge_names[i]));
                continue;
            }
            result.add(new EdgeVerifier(x, y, z, i, color));
        }
        LOGGER.debug("");

        return result;
    }
}
