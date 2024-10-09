// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.graphics;

import com.midnightbits.scanner.rt.core.ScannerMod;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.sonar.BlockEcho;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TriColorSorter implements Iterator<Map<V3i, EchoState>>, Iterable<Map<V3i, EchoState>> {
    private static final String TAG = ScannerMod.MOD_ID;
    private static final Logger LOGGER = LoggerFactory.getLogger(TAG);

    private record AdjacentSides(int x, int y, int z, int mine, int theirs) {
    }

    private static final AdjacentSides[] ADJACENT_SIDES = new AdjacentSides[] {
            new AdjacentSides(-1, 0, 0, Pixel.SIDE_X0, Pixel.SIDE_X1),
            new AdjacentSides(1, 0, 0, Pixel.SIDE_X1, Pixel.SIDE_X0),
            new AdjacentSides(0, -1, 0, Pixel.SIDE_Y0, Pixel.SIDE_Y1),
            new AdjacentSides(0, 1, 0, Pixel.SIDE_Y1, Pixel.SIDE_Y0),
            new AdjacentSides(0, 0, -1, Pixel.SIDE_Z0, Pixel.SIDE_Z1),
            new AdjacentSides(0, 0, 1, Pixel.SIDE_Z1, Pixel.SIDE_Z0),
    };

    private final Map<V3i, EchoState> pool = new HashMap<>();
    private final Deque<V3i> white;

    public TriColorSorter(Stream<EchoState> echoes) {
        echoes.forEach((echo) -> pool.put(echo.position(), echo));
        white = pool.keySet().stream().sorted().collect(Collectors.toCollection(ArrayDeque::new));
    }

    public TriColorSorter(Collection<BlockEcho> echoes) {
        this(echoes.stream().map(EchoState::new));
    }

    public boolean hasNext() {
        return !white.isEmpty();
    }

    private Map<V3i, EchoState> moveFromWhiteToBlack() {
        Deque<EchoState> grey = new ArrayDeque<>();
        Map<V3i, EchoState> black = new HashMap<>();

        final var nextWhite = pool.get(white.removeFirst());
        grey.add(nextWhite);
        while (!grey.isEmpty()) {
            EchoState current = grey.removeFirst();
            white.remove(current.position());
            black.put(current.position(), current);

            for (final var side : ADJACENT_SIDES) {
                if ((current.sides & side.mine) == 0) {
                    continue;
                }

                final var neighbour = pool.get(current.position().add(side.x, side.y, side.z));
                if (neighbour == null || !neighbour.id().equals(current.id())) {
                    continue;
                }

                current.sides &= ~side.mine;
                neighbour.sides &= ~side.theirs;
                grey.add(neighbour);
            }
        }

        return black;
    }

    public Map<V3i, EchoState> next() {
        final var black = moveFromWhiteToBlack();

        final var sortedPos = black.keySet().stream().sorted().toList();

        for (final var pos : sortedPos) {
            final var pixel = black.get(pos);

            pixel.edges = 0;
            for (int edgeIndex = 0; edgeIndex < Pixel.edges.length; ++edgeIndex) {
                final var edge = Pixel.edges[edgeIndex];

                final int mask = edge.validSides(pixel.sides);
                if (mask == edge.sides()) {
                    pixel.edges |= 1 << edgeIndex;
                } else if (mask != 0) {
                    final var oppositeIndex = edge.opposite();
                    final var oppositeEdge = Pixel.edges[oppositeIndex];
                    final var movement = edge.start().sub(oppositeEdge.start());
                    final var oppositePos = pixel.position().add(movement.x(), movement.y(), movement.z());
                    final var opposite = black.get(oppositePos);

                    if (opposite == null) {
                        continue;
                    }

                    final int oppositeBit = 1 << oppositeIndex;
                    if ((opposite.edges & oppositeBit) == 0) {
                        pixel.edges |= 1 << edgeIndex;
                        LOGGER.debug("{}",
                                String.format(
                                        "[%s] %2d (%d%d%d -> %d%d%d): %02x -> %2d -> (%d, %d, %d) -> %x/%x -> adding",
                                        pos, edgeIndex,
                                        edge.start().x(), edge.start().y(), edge.start().z(),
                                        edge.end().x(), edge.end().y(), edge.end().z(),
                                        edge.sides() - mask, oppositeIndex,
                                        movement.x(), movement.y(), movement.z(),
                                        opposite.edges,
                                        opposite.edges & oppositeBit));

                    }
                }
            }
        }

        return black;
    }

    @Override
    public @NotNull Iterator<Map<V3i, EchoState>> iterator() {
        return this;
    }
}
