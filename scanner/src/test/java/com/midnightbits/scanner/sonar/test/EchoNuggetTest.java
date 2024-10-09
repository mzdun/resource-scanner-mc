// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.test;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.sonar.BlockEcho;
import com.midnightbits.scanner.sonar.Echo;
import com.midnightbits.scanner.sonar.EchoNugget;
import com.midnightbits.scanner.sonar.graphics.Colors;
import com.midnightbits.scanner.sonar.graphics.Pixel;
import com.midnightbits.scanner.utils.test.gl.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class EchoNuggetTest {
    public static final Colors.Proxy VANILLA = new Colors.DirectValue(Colors.VANILLA);

    private static Echo of(String path) {
        return Echo.of(Id.ofVanilla(path), Colors.BLOCK_TAG_COLORS.getOrDefault(Id.of(path + "s"), VANILLA));
    }

    final static Echo gold_ore = of("gold_ore");
    final static Echo coal_ore = of("coal_ore");
    final static Echo iron_ore = of("iron_ore");

    final static int EDGES_X0 = Pixel.EDGE_FRONT_LEFT | Pixel.EDGE_BACK_LEFT | Pixel.EDGE_LEFT_BOTTOM
            | Pixel.EDGE_LEFT_TOP;
    final static int EDGES_X1 = Pixel.EDGE_FRONT_RIGHT | Pixel.EDGE_BACK_RIGHT | Pixel.EDGE_RIGHT_BOTTOM
            | Pixel.EDGE_RIGHT_TOP;
    final static int EDGES_Y0 = Pixel.EDGE_FRONT_BOTTOM | Pixel.EDGE_BACK_BOTTOM | Pixel.EDGE_LEFT_BOTTOM
            | Pixel.EDGE_RIGHT_BOTTOM;
    final static int EDGES_Y1 = Pixel.EDGE_FRONT_TOP | Pixel.EDGE_BACK_TOP | Pixel.EDGE_LEFT_TOP
            | Pixel.EDGE_RIGHT_TOP;
    final static int EDGES_Z0 = Pixel.EDGE_FRONT_BOTTOM | Pixel.EDGE_FRONT_TOP | Pixel.EDGE_FRONT_LEFT
            | Pixel.EDGE_FRONT_RIGHT;
    final static int EDGES_Z1 = Pixel.EDGE_BACK_BOTTOM | Pixel.EDGE_BACK_TOP | Pixel.EDGE_BACK_LEFT
            | Pixel.EDGE_BACK_RIGHT;

    @Test
    void oppositeEdgesAreStable() {
        for (int edgeId = 0; edgeId < Pixel.edges.length; ++edgeId) {
            final var edge = Pixel.edges[edgeId];
            final var oppo = Pixel.edges[edge.opposite()];
            final var start = edge.start().sub(oppo.start());
            final var end = edge.end().sub(oppo.end());
            final var msg = String.format("%2d %d%d%d:%d%d%d -> %2d %d%d%d:%d%d%d (%d, %d, %d)/(%d, %d, %d) %n", edgeId,
                    edge.start().x(), edge.start().y(), edge.start().z(),
                    edge.end().x(), edge.end().y(), edge.end().z(),
                    edge.opposite(),
                    oppo.start().x(), oppo.start().y(), oppo.start().z(),
                    oppo.end().x(), oppo.end().y(), oppo.end().z(),
                    start.x(), start.y(), start.z(),
                    end.x(), end.y(), end.z());
            Assertions.assertEquals(start, end, msg);
            Assertions.assertEquals(edgeId, oppo.opposite(), msg);
        }
    }

    @Test
    void disjointedEchoes() {
        final var nuggets = EchoNugget.group(Set.of(
                BlockEcho.echoFrom(0, 0, 0, gold_ore),
                BlockEcho.echoFrom(2, 0, 0, gold_ore),
                BlockEcho.echoFrom(1, 1, 0, gold_ore)));

        final var tape = VertexTape.record(nuggets);

        tape.assertPlayback(new VertexVerifier[] {
                new BlockVerifier(5, 0, 0, Pixel.ALL_SIDES, 0xFAEE4D),
                new MarkerVerifier(0x000000),
                new BlockVerifier(4, 1, 0, Pixel.ALL_SIDES, 0xFAEE4D),
                new MarkerVerifier(0x000000),
                new BlockVerifier(3, 0, 0, Pixel.ALL_SIDES, 0xFAEE4D),
                new MarkerVerifier(0x000000),
                new MarkerVerifier(0xFFFFFF),
                new FrameVerifier(5, 0, 0, Pixel.ALL_EDGES, 0xFAEE4D),
                new MarkerVerifier(0xFFFFFF),
                new FrameVerifier(4, 1, 0, Pixel.ALL_EDGES, 0xFAEE4D),
                new MarkerVerifier(0xFFFFFF),
                new FrameVerifier(3, 0, 0, Pixel.ALL_EDGES, 0xFAEE4D),
        });
    }

    @Test
    void semiDetachedEchos() {
        final var nuggets = EchoNugget.group(Set.of(
                BlockEcho.echoFrom(0, 0, 0, gold_ore),
                BlockEcho.echoFrom(1, 0, 0, iron_ore)));

        final var tape = VertexTape.record(nuggets);

        tape.assertPlayback(new VertexVerifier[] {
                new BlockVerifier(4, 0, 0, Pixel.ALL_SIDES, 0x664C33),
                new MarkerVerifier(0x000000),
                new BlockVerifier(3, 0, 0, Pixel.ALL_SIDES, 0xFAEE4D),
                new MarkerVerifier(0x000000),
                new MarkerVerifier(0xFFFFFF),
                new FrameVerifier(4, 0, 0, Pixel.ALL_EDGES, 0x664C33),
                new MarkerVerifier(0xFFFFFF),
                new FrameVerifier(3, 0, 0, Pixel.ALL_EDGES, 0xFAEE4D),
        });
    }

    @Test
    void attachedEchosX() {
        final var nuggets = EchoNugget.group(Set.of(
                BlockEcho.echoFrom(0, 0, 0, coal_ore),
                BlockEcho.echoFrom(1, 0, 0, coal_ore)));

        final var tape = VertexTape.record(nuggets);

        tape.assertPlayback(new VertexVerifier[] {
                new BlockVerifier(4, 0, 0, Pixel.ALL_SIDES & ~Pixel.SIDE_X0, 0x4C4C4C),
                new BlockVerifier(3, 0, 0, Pixel.ALL_SIDES & ~Pixel.SIDE_X1, 0x4C4C4C),
                new MarkerVerifier(0x000000),
                new MarkerVerifier(0xFFFFFF),
                new FrameVerifier(4, 0, 0, Pixel.ALL_EDGES & ~EDGES_X0, 0x4C4C4C),
                new FrameVerifier(3, 0, 0, Pixel.ALL_EDGES & ~EDGES_X1, 0x4C4C4C),
        });
    }

    @Test
    void attachedEchosY() {
        final var nuggets = EchoNugget.group(Set.of(
                BlockEcho.echoFrom(0, 0, 0, coal_ore),
                BlockEcho.echoFrom(0, 1, 0, coal_ore)));

        final var tape = VertexTape.record(nuggets);

        tape.assertPlayback(new VertexVerifier[] {
                new BlockVerifier(3, 1, 0, Pixel.ALL_SIDES & ~Pixel.SIDE_Y0, 0x4C4C4C),
                new BlockVerifier(3, 0, 0, Pixel.ALL_SIDES & ~Pixel.SIDE_Y1, 0x4C4C4C),
                new MarkerVerifier(0x000000),
                new MarkerVerifier(0xFFFFFF),
                new FrameVerifier(3, 1, 0, Pixel.ALL_EDGES & ~EDGES_Y0, 0x4C4C4C),
                new FrameVerifier(3, 0, 0, Pixel.ALL_EDGES & ~EDGES_Y1, 0x4C4C4C),
        });
    }

    @Test
    void attachedEchosZ() {
        final var nuggets = EchoNugget.group(Set.of(
                BlockEcho.echoFrom(0, 0, 0, coal_ore),
                BlockEcho.echoFrom(0, 0, 1, coal_ore)));

        final var tape = VertexTape.record(nuggets);

        tape.assertPlayback(new VertexVerifier[] {
                new BlockVerifier(3, 0, 1, Pixel.ALL_SIDES & ~Pixel.SIDE_Z0, 0x4C4C4C),
                new BlockVerifier(3, 0, 0, Pixel.ALL_SIDES & ~Pixel.SIDE_Z1, 0x4C4C4C),
                new MarkerVerifier(0x000000),
                new MarkerVerifier(0xFFFFFF),
                new FrameVerifier(3, 0, 1, Pixel.ALL_EDGES & ~EDGES_Z0, 0x4C4C4C),
                new FrameVerifier(3, 0, 0, Pixel.ALL_EDGES & ~EDGES_Z1, 0x4C4C4C),
        });
    }

    @Test
    void attachedEchosXY() {
        final var nuggets = EchoNugget.group(Set.of(
                BlockEcho.echoFrom(0, 0, 0, coal_ore),
                BlockEcho.echoFrom(1, 0, 0, coal_ore),
                BlockEcho.echoFrom(0, 1, 0, coal_ore),
                BlockEcho.echoFrom(1, 1, 0, coal_ore)));

        final var tape = VertexTape.record(nuggets);

        tape.assertPlayback(new VertexVerifier[] {
                new BlockVerifier(4, 1, 0, Pixel.ALL_SIDES & ~(Pixel.SIDE_X0 | Pixel.SIDE_Y0), 0x4C4C4C),
                new BlockVerifier(4, 0, 0, Pixel.ALL_SIDES & ~(Pixel.SIDE_X0 | Pixel.SIDE_Y1), 0x4C4C4C),
                new BlockVerifier(3, 1, 0, Pixel.ALL_SIDES & ~(Pixel.SIDE_X1 | Pixel.SIDE_Y0), 0x4C4C4C),
                new BlockVerifier(3, 0, 0, Pixel.ALL_SIDES & ~(Pixel.SIDE_X1 | Pixel.SIDE_Y1), 0x4C4C4C),
                new MarkerVerifier(0x000000),
                new MarkerVerifier(0xFFFFFF),
                new FrameVerifier(4, 1, 0, Pixel.ALL_EDGES & ~(EDGES_X0 | EDGES_Y0), 0x4C4C4C),
                new FrameVerifier(4, 0, 0, Pixel.ALL_EDGES & ~(EDGES_X0 | EDGES_Y1), 0x4C4C4C),
                new FrameVerifier(3, 1, 0, Pixel.ALL_EDGES & ~(EDGES_X1 | EDGES_Y0), 0x4C4C4C),
                new FrameVerifier(3, 0, 0, Pixel.ALL_EDGES & ~(EDGES_X1 | EDGES_Y1), 0x4C4C4C),
        });
    }

    @Test
    void tetromino() {
        final var nuggets = EchoNugget.group(Set.of(
                BlockEcho.echoFrom(0, 0, 0, coal_ore),
                BlockEcho.echoFrom(1, 0, 0, coal_ore),
                BlockEcho.echoFrom(1, 1, 0, coal_ore),
                BlockEcho.echoFrom(2, 0, 0, coal_ore)));

        final var tape = VertexTape.record(nuggets);

        tape.assertPlayback(new VertexVerifier[] {
                new BlockVerifier(5, 0, 0, Pixel.ALL_SIDES & ~Pixel.SIDE_X0, 0x4C4C4C),
                new BlockVerifier(4, 1, 0, Pixel.ALL_SIDES & ~Pixel.SIDE_Y0, 0x4C4C4C),
                new BlockVerifier(4, 0, 0, Pixel.SIDE_Y0 | Pixel.SIDE_Z0 | Pixel.SIDE_Z1, 0x4C4C4C),
                new BlockVerifier(3, 0, 0, Pixel.ALL_SIDES & ~Pixel.SIDE_X1, 0x4C4C4C),
                new MarkerVerifier(0x000000),
                new MarkerVerifier(0xFFFFFF),
                new FrameVerifier(5, 0, 0, Pixel.ALL_EDGES & ~EDGES_X0 | Pixel.EDGE_LEFT_TOP, 0x4C4C4C),
                new FrameVerifier(4, 1, 0, Pixel.ALL_EDGES & ~EDGES_Y0, 0x4C4C4C),
                new FrameVerifier(4, 0, 0, Pixel.EDGE_FRONT_BOTTOM | Pixel.EDGE_BACK_BOTTOM, 0x4C4C4C),
                new FrameVerifier(3, 0, 0, Pixel.ALL_EDGES & ~EDGES_X1 | Pixel.EDGE_RIGHT_TOP, 0x4C4C4C),
        });
    }

    @Test
    void largeL() {
        final var nuggets = EchoNugget.group(Set.of(
                BlockEcho.echoFrom(0, 0, 0, coal_ore),
                BlockEcho.echoFrom(1, 0, 0, coal_ore),
                BlockEcho.echoFrom(0, 1, 0, coal_ore),
                BlockEcho.echoFrom(0, 0, 1, coal_ore),
                BlockEcho.echoFrom(1, 0, 1, coal_ore),
                BlockEcho.echoFrom(0, 1, 1, coal_ore)));

        final var tape = VertexTape.record(nuggets);

        tape.assertPlayback(new VertexVerifier[] {
                new BlockVerifier(4, 0, 1, Pixel.ALL_SIDES & ~(Pixel.SIDE_X0 | Pixel.SIDE_Z0), 0x4C4C4C),
                new BlockVerifier(4, 0, 0, Pixel.ALL_SIDES & ~(Pixel.SIDE_X0 | Pixel.SIDE_Z1), 0x4C4C4C),
                new BlockVerifier(3, 1, 1, Pixel.ALL_SIDES & ~(Pixel.SIDE_Y0 | Pixel.SIDE_Z0), 0x4C4C4C),
                new BlockVerifier(3, 0, 1, Pixel.SIDE_X0 | Pixel.SIDE_Y0 | Pixel.SIDE_Z1, 0x4C4C4C),
                new BlockVerifier(3, 1, 0, Pixel.ALL_SIDES & ~(Pixel.SIDE_Y0 | Pixel.SIDE_Z1), 0x4C4C4C),
                new BlockVerifier(3, 0, 0, Pixel.SIDE_X0 | Pixel.SIDE_Y0 | Pixel.SIDE_Z0, 0x4C4C4C),
                new MarkerVerifier(0x000000),
                new MarkerVerifier(0xFFFFFF),
                new FrameVerifier(4, 0, 1, Pixel.ALL_EDGES & ~(EDGES_X0 | EDGES_Z0) | Pixel.EDGE_LEFT_TOP, 0x4C4C4C),
                new FrameVerifier(4, 0, 0, Pixel.ALL_EDGES & ~(EDGES_X0 | EDGES_Z1) | Pixel.EDGE_LEFT_TOP, 0x4C4C4C),
                new FrameVerifier(3, 1, 1, Pixel.ALL_EDGES & ~(EDGES_Y0 | EDGES_Z0), 0x4C4C4C),
                new FrameVerifier(3, 0, 1, Pixel.ALL_EDGES & ~(EDGES_X1 | EDGES_Y1 | EDGES_Z0), 0x4C4C4C),
                new FrameVerifier(3, 1, 0, Pixel.ALL_EDGES & ~(EDGES_Y0 | EDGES_Z1), 0x4C4C4C),
                new FrameVerifier(3, 0, 0, Pixel.ALL_EDGES & ~(EDGES_X1 | EDGES_Y1 | EDGES_Z1), 0x4C4C4C),
        });
    }
}
