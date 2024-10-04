// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils;

import java.util.*;

import com.midnightbits.scanner.rt.math.V3d;
import com.midnightbits.scanner.rt.math.V3i;
import org.jetbrains.annotations.NotNull;

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

    public Slicer sliced() {
        return new Slicer(this);
    }

    public static final class Slicer implements Iterator<RangedPositions>, Iterable<RangedPositions> {
        public static int PRECISION = 4;
        int sliceStart = 0;
        int sliceDistance = 0;
        List<RangedPosition> items = new ArrayList<>();

        Slicer(ConeOfBlocks cone) {
            final Set<V3i> seen = new TreeSet<>();
            for (final var line : cone.iterate()) {
                for (final var pos : line.iterate()) {
                    if (!seen.add(pos))
                        continue;
                    final var distanceSquared = cone.operator.getSquaredDistance(pos);
                    final var distance = (int) Math.round(Math.sqrt(distanceSquared) * PRECISION);
                    items.add(new RangedPosition(distance, pos));
                }
            }
            items.sort(Comparator.comparingInt(p -> p.distance));
            // items.forEach(ranged -> ResourceScannerMod.LOGGER.warn("++ {}: {}",
            // ranged.distance, ranged.pos));
        }

        @NotNull
        @Override
        public Iterator<RangedPositions> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return sliceStart < items.size();
        }

        @Override
        public RangedPositions next() {
            // ResourceScannerMod.LOGGER.warn("NEXT {} {}", sliceDistance, sliceStart);
            for (int index = sliceStart; index < items.size(); ++index) {
                final var ranged = items.get(index);
                if (sliceDistance != ranged.distance && sliceStart < index) {
                    final var result = getSlice(items, sliceStart, index);
                    sliceDistance = ranged.distance;
                    sliceStart = index;
                    return result;
                }
            }
            final var result = getSlice(items, sliceStart, items.size());
            sliceStart = items.size();
            return result;
        }

        private RangedPositions getSlice(List<RangedPosition> allBlocks, int sliceStartIncl, int sliceEndExcl) {
            // ResourceScannerMod.LOGGER.warn(" TAKE {} [{}, {}), {}", sliceDistance,
            // sliceStartIncl, sliceEndExcl, sliceEndExcl - sliceStartIncl);
            return new RangedPositions(sliceDistance, allBlocks.stream()
                    .skip(sliceStartIncl)
                    .limit(sliceEndExcl - sliceStartIncl)
                    .map(range -> range.pos)
                    .toList());
        }
    }

    private record RangedPosition(int distance, V3i pos) {
    }

    public record RangedPositions(int distance, List<V3i> items) {
    }
}
