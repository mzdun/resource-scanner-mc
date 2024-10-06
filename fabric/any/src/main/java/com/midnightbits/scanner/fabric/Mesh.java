package com.midnightbits.scanner.fabric;

import com.midnightbits.scanner.rt.math.V3i;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class Mesh {
    public static void cleanPixels(Collection<Pixel> pixels) {
        final Map<V3i, Pixel> vertices = new HashMap<>();

        for (final var pixel : pixels) {
            final var control = vertices.get(pixel.position());
            if (control != null && control != pixel) {
                pixel.sides = 0;
                return;
            }

            vertices.put(pixel.position(), pixel);
        }

        final var positions = Set.copyOf(vertices.keySet());
        for (final var pos: positions) {
            final var pixel = vertices.get(pos);
            if (pixel == null) { continue; }

            final var x0 = vertices.get(pixel.position().add(-1, 0, 0));
            final var x1 = vertices.get(pixel.position().add(1, 0, 0));
            final var y0 = vertices.get(pixel.position().add(0, -1, 0));
            final var y1 = vertices.get(pixel.position().add(0, 1, 0));
            final var z0 = vertices.get(pixel.position().add(0, 0, -1));
            final var z1 = vertices.get(pixel.position().add(0, 0, 1));

            cleanSide(pixel, x0, Pixel.SIDE_X0, Pixel.SIDE_X1);
            cleanSide(pixel, x1, Pixel.SIDE_X1, Pixel.SIDE_X0);
            cleanSide(pixel, y0, Pixel.SIDE_Y0, Pixel.SIDE_Y1);
            cleanSide(pixel, y1, Pixel.SIDE_Y1, Pixel.SIDE_Y0);
            cleanSide(pixel, z0, Pixel.SIDE_Z0, Pixel.SIDE_Z1);
            cleanSide(pixel, z1, Pixel.SIDE_Z1, Pixel.SIDE_Z0);

            vertices.remove(pos);
        }
    }

    private static void cleanSide(Pixel pixel, Pixel neighbour, int mySide, int theirSide) {
        if (neighbour == null || neighbour.argb != pixel.argb) {
            return;
        }
        pixel.sides &= ~mySide;
        neighbour.sides &= ~theirSide;
    }
}
