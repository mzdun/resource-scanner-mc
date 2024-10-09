// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils.test.gl;

import com.midnightbits.scanner.sonar.graphics.Pixel;

import java.util.ArrayList;
import java.util.List;

public class BlockVerifier extends SubVerifier {
    public BlockVerifier(int x, int y, int z, int sides, int color) {
        super(makeSides(x, y, z, sides, color),
                String.format("BlockVerifier(%d, %d, %d, 0x%x, 0x%X)", x, y, z, sides, color));
    }

    private static List<VertexVerifier> makeSides(int x, int y, int z, int sides, int color) {
        List<VertexVerifier> result = new ArrayList<>();
        for (int i = 0; i < Pixel.triangles.length; ++i) {
            final int flag = 1 << i;
            if ((sides & flag) == 0) {
                VertexVerifier.LOGGER.debug(String.format("BlockVerifier(%d, %d, %d, 0x%x, 0x%X): skipping %d/%s", x, y,
                        z, sides, color, i, Pixel.side_names[i]));
                continue;
            }
            result.add(new FaceVerifier(x, y, z, i, color));
        }
        LOGGER.debug("");

        return result;
    }
}
