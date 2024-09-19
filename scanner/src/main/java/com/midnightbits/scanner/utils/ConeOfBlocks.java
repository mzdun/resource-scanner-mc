package com.midnightbits.scanner.utils;

import java.util.Optional;

import com.midnightbits.scanner.rt.math.V3d;
import com.midnightbits.scanner.rt.math.V3i;

public final class ConeOfBlocks {
    final V3i operator;
    final V3i offset;
    final int radius;

    public static ConeOfBlocks fromCamera(V3i operator, V3d camera, int distance, int radius) {
        final var offset = V3i.ofRounded(camera.multiply(distance));
        return new ConeOfBlocks(operator, offset, radius);
    }

    public static ConeOfBlocks fromCamera(V3i operator, float cameraPitch, float cameraYaw, int distance, int radius) {
        return fromCamera(operator, V3d.fromPolar(cameraPitch, cameraYaw), distance, radius);
    }

    public ConeOfBlocks(V3i operator, V3i offset, int radius) {
        this.operator = operator;
        this.offset = offset;
        this.radius = radius;
    }

    public Iterable<LineOfBlocks> iterate() {
        final var plate = new Circle(radius).iterateAlongCamera(offset).iterator();
        final var tgt = operator.add(offset);
        final Callback<LineOfBlocks> cb = () -> {
            if (!plate.hasNext())
                return Optional.empty();

            final var pt = plate.next();
            return Optional.of(new LineOfBlocks(operator, tgt.add(pt)));
        };
        return CallbackIterable.of(cb);
    }

}
