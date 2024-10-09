// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.graphics;

import com.midnightbits.scanner.sonar.EchoState;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public record Pixel(EchoState echoState, double distanceSquared) {

    public static Pixel of(EchoState echoState, Vector3f camera) {
        final var myPos = echoState.position();
        final var pos = new Vector3f(myPos.getX(), myPos.getY(), myPos.getZ()).add(.5F, .5F, .5F);
        return new Pixel(echoState, pos.distanceSquared(camera));
    }

    public record Vertex(int x, int y, int z) {
        public void apply(GlProgramConsumer buffer, Matrix4f matrix, int argb32) {
            buffer.vertexColor(matrix, x, y, z, argb32);
        }

        public Vertex sub(Vertex start) {
            return new Vertex(x - start.x, y - start.y, z - start.z);
        }
    }

    public static final Vertex v000 = new Vertex(0, 0, 0);
    public static final Vertex v001 = new Vertex(0, 0, 1);
    public static final Vertex v010 = new Vertex(0, 1, 0);
    public static final Vertex v011 = new Vertex(0, 1, 1);
    public static final Vertex v100 = new Vertex(1, 0, 0);
    public static final Vertex v101 = new Vertex(1, 0, 1);
    public static final Vertex v110 = new Vertex(1, 1, 0);
    public static final Vertex v111 = new Vertex(1, 1, 1);

    public static final Vertex[][] triangles = new Vertex[][] {
            new Vertex[] { v000, v010, v100, v100, v010, v110 },
            new Vertex[] { v001, v000, v101, v101, v000, v100 },
            new Vertex[] { v011, v001, v111, v111, v001, v101 },
            new Vertex[] { v010, v011, v110, v110, v011, v111 },
            new Vertex[] { v001, v011, v000, v000, v011, v010 },
            new Vertex[] { v100, v110, v101, v101, v110, v111 },
    };

    public final static int SIDE_Z0 = 1;
    public final static int SIDE_Y0 = 1 << 1;
    public final static int SIDE_Z1 = 1 << 2;
    public final static int SIDE_Y1 = 1 << 3;
    public final static int SIDE_X0 = 1 << 4;
    public final static int SIDE_X1 = 1 << 5;
    public final static int ALL_SIDES = SIDE_X0 | SIDE_X1 | SIDE_Y0 | SIDE_Y1 | SIDE_Z0 | SIDE_Z1;

    public final static String[] side_names = new String[] {
            "Z0",
            "Y0",
            "Z1",
            "Y1",
            "X0",
            "X1",
    };

    public record Edge(Vertex start, Vertex end, int sides, int opposite) {
        public void apply(GlProgramConsumer buffer, Matrix4f matrix, int argb) {
            start.apply(buffer, matrix, argb);
            end.apply(buffer, matrix, argb);
        }

        public int validSides(int blockSides) {
            return blockSides & sides;
        }
    }

    public static final Edge[] edges = new Edge[] {
            /* 0 */
            new Edge(v000, v100, SIDE_Z0 | SIDE_Y0, 2),
            new Edge(v001, v101, SIDE_Y0 | SIDE_Z1, 3),
            new Edge(v011, v111, SIDE_Z1 | SIDE_Y1, 0),
            new Edge(v010, v110, SIDE_Z0 | SIDE_Y1, 1),

            /* 4 */
            new Edge(v000, v010, SIDE_Z0 | SIDE_X0, 6),
            new Edge(v001, v011, SIDE_Z1 | SIDE_X0, 7),
            new Edge(v101, v111, SIDE_Z1 | SIDE_X1, 4),
            new Edge(v100, v110, SIDE_Z0 | SIDE_X1, 5),

            /* 8 */
            new Edge(v000, v001, SIDE_Y0 | SIDE_X0, 10),
            new Edge(v010, v011, SIDE_Y1 | SIDE_X0, 11),
            new Edge(v110, v111, SIDE_Y1 | SIDE_X1, 8),
            new Edge(v100, v101, SIDE_Y0 | SIDE_X1, 9),
    };

    public final static int EDGE_FRONT_BOTTOM = 1;
    public final static int EDGE_BACK_BOTTOM = 1 << 1;
    public final static int EDGE_BACK_TOP = 1 << 2;
    public final static int EDGE_FRONT_TOP = 1 << 3;

    public final static int EDGE_FRONT_LEFT = 1 << 4;
    public final static int EDGE_BACK_LEFT = 1 << 5; // our left; if you turned the echo around, it would be back right
    public final static int EDGE_BACK_RIGHT = 1 << 6; // same
    public final static int EDGE_FRONT_RIGHT = 1 << 7;

    public final static int EDGE_LEFT_BOTTOM = 1 << 8;
    public final static int EDGE_LEFT_TOP = 1 << 9;
    public final static int EDGE_RIGHT_TOP = 1 << 10;
    public final static int EDGE_RIGHT_BOTTOM = 1 << 11;

    public final static String[] edge_names = new String[] {
            "FRONT_BOTTOM",
            "BACK_BOTTOM",
            "BACK_TOP",
            "FRONT_TOP",

            "FRONT_LEFT",
            "BACK_LEFT",
            "BACK_RIGHT",
            "FRONT_RIGHT",

            "LEFT_BOTTOM",
            "LEFT_TOP",
            "RIGHT_TOP",
            "RIGHT_BOTTOM",
    };

    public final static int ALL_EDGES = EDGE_FRONT_BOTTOM | EDGE_BACK_BOTTOM | EDGE_BACK_TOP | EDGE_FRONT_TOP |
            EDGE_FRONT_LEFT | EDGE_BACK_LEFT | EDGE_BACK_RIGHT | EDGE_FRONT_RIGHT |
            EDGE_LEFT_BOTTOM | EDGE_LEFT_TOP | EDGE_RIGHT_BOTTOM | EDGE_RIGHT_TOP;
}
