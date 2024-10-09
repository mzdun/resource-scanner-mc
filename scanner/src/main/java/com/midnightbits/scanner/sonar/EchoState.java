// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.core.ScannerMod;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.sonar.graphics.Colors;
import com.midnightbits.scanner.sonar.graphics.GlProgramConsumer;
import com.midnightbits.scanner.sonar.graphics.MatrixStack;
import com.midnightbits.scanner.sonar.graphics.Pixel;
import com.midnightbits.scanner.utils.Clock;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoState implements Comparable<EchoState> {
    private static final String TAG = ScannerMod.MOD_ID;
    private static final Logger LOGGER = LoggerFactory.getLogger(TAG);

    public record Partial(V3i position, Echo echo) {
        public static Partial of(V3i position, Echo echo) {
            return new Partial(position, echo);
        }

        public static Partial of(int x, int y, int z, Echo echo) {
            return of(new V3i(x, y, z), echo);
        }

        public Id id() {
            return echo.id();
        }

        public boolean equals(@NotNull Object obj) {
            if (!(obj instanceof Partial other)) {
                throw new ClassCastException();
            }
            return echo.equals(other.echo) && position.equals(other.position);
        }

        @Override
        public String toString() {
            return "EchoState.Partial.of(" +
                    position.getX() + ", " +
                    position.getY() + ", " +
                    position.getZ() + ", " +
                    echo + ")";
        }
    }

    final V3i position;
    final Echo echo;
    final long pingTime;
    public int sides;
    public int edges;
    public int alpha;

    public EchoState(V3i position, Echo echo, long pingTime, int sides, int edges, int alpha) {
        this.position = position;
        this.echo = echo;
        this.pingTime = pingTime;
        this.sides = sides;
        this.edges = edges;
        this.alpha = alpha;
    }

    public EchoState(V3i position, Echo echo, long pingTime) {
        this(position, echo, pingTime, Pixel.ALL_SIDES, 0, Colors.ECHO_ALPHA);
    }

    public EchoState(int x, int y, int z, Echo echo, long pingTime, int sides, int edges, int alpha) {
        this(new V3i(x, y, z), echo, pingTime, sides, edges, alpha);
    }

    public EchoState(int x, int y, int z, Echo echo, long pingTime) {
        this(new V3i(x, y, z), echo, pingTime);
    }

    public EchoState withAllEdges() {
        edges = Pixel.ALL_EDGES;
        return this;
    }

    public static EchoState echoFrom(Partial partial) {
        return new EchoState(partial.position, partial.echo, Clock.currentTimeMillis());
    }

    public static EchoState echoFrom(int x, int y, int z, Echo echo) {
        return echoFrom(Partial.of(x, y, z, echo));
    }

    public V3i position() {
        return position;
    }

    public Echo echo() {
        return echo;
    }

    public long pingTime() {
        return pingTime;
    }

    public Id id() {
        return echo.id();
    }

    public Colors.Proxy color() {
        return echo.color();
    }

    public void draw(GlProgramConsumer buffer, MatrixStack matrices, Vector3f camera) {
        final var m = pushAndAlignWithCamera(matrices, camera);

        if (alpha == 0) {
            return;
        }

        final var argb32 = alpha | color().rgb24();

        for (var side = 0; side < Pixel.triangles.length; ++side) {
            final var flag = 1 << side;
            if ((sides & flag) == 0) {
                LOGGER.debug("[{}]: skipping: {}/{}", position, side, Pixel.side_names[side]);
                continue;
            }

            final var wall = Pixel.triangles[side];
            for (final var vertex : wall) {
                vertex.apply(buffer, m, argb32);
            }
        }
        LOGGER.debug("");

        matrices.pop();
    }

    public void sketch(GlProgramConsumer buffer, MatrixStack matrices, Vector3f camera) {
        final var m = pushAndAlignWithCamera(matrices, camera);

        final var validColor = Colors.OPAQUE | color().rgb24();

        for (var index = 0; index < Pixel.edges.length; ++index) {
            final var flag = 1 << index;
            if ((edges & flag) == 0) {
                LOGGER.debug("[{}]: skipping: {}/{}", position, index, Pixel.edge_names[index]);
                continue;
            }

            Pixel.edges[index].apply(buffer, m, validColor);
        }
        LOGGER.debug("");

        matrices.pop();
    }

    private Matrix4f pushAndAlignWithCamera(MatrixStack matrices, Vector3f camera) {
        final var x = position().getX() - camera.x;
        final var y = position().getY() - camera.y;
        final var z = position().getZ() - camera.z;
        matrices.push();
        matrices.translate(x, y, z);
        return matrices.peek().getPositionMatrix();
    }

    public AABB getBounds() {
        double minX = position.getX();
        double minY = position.getY();
        double minZ = position.getZ();

        return new AABB(minX, minY, minZ, minX + 1, minY + 1, minZ + 1);
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (!(obj instanceof EchoState other)) {
            throw new ClassCastException();
        }
        return pingTime == other.pingTime &&
               echo.equals(other.echo) &&
               position.equals(other.position) &&
               sides == other.sides &&
               edges == other.edges &&
               alpha == other.alpha;
    }

    @Override
    public String toString() {
        final var prefix = "\nnew EchoState(" +
                position.getX() + ", " +
                position.getY() + ", " +
                position.getZ() + ", " +
                echo + ", " +
                pingTime;
        final var states = (sides != Pixel.ALL_SIDES || edges != 0 || alpha != Colors.ECHO_ALPHA)
                ? (sides == Pixel.ALL_SIDES && edges == Pixel.ALL_EDGES && alpha == Colors.ECHO_ALPHA)
                        ? ").withAllEdges("
                        : String.format(", %s, %s, %s",
                                sides == Pixel.ALL_SIDES ? "Pixel.ALL_SIDES" : String.format("0x%x", sides),
                                edges == Pixel.ALL_EDGES ? "Pixel.ALL_EDGES" : String.format("0x%x", edges),
                                alpha == Colors.ECHO_ALPHA ? "Colors.ECHO_ALPHA" : String.format("0x%08X", alpha))
                : "";

        return prefix + states + ")";
    }

    @Override
    public int compareTo(@NotNull EchoState other) {
        var result = (int) (pingTime - other.pingTime);
        if (result != 0) {
            return result;
        }

        result = echo.compareTo(other.echo);
        if (result != 0)
            return result;

        result = position.compareTo(other.position);
        if (result != 0)
            return result;

        result = sides - other.sides;
        if (result != 0)
            return result;

        result = edges - other.edges;
        if (result != 0)
            return result;

        return alpha - other.alpha;
    }

    public static class AABB {
        private double minX;
        private double minY;
        private double minZ;
        private double maxX;
        private double maxY;
        private double maxZ;

        public AABB(double x1, double y1, double z1, double x2, double y2, double z2) {
            this.minX = Math.min(x1, x2);
            this.minY = Math.min(y1, y2);
            this.minZ = Math.min(z1, z2);
            this.maxX = Math.max(x1, x2);
            this.maxY = Math.max(y1, y2);
            this.maxZ = Math.max(z1, z2);
        }

        public double minX() {
            return minX;
        }

        public double minY() {
            return minY;
        }

        public double minZ() {
            return minZ;
        }

        public double maxX() {
            return maxX;
        }

        public double maxY() {
            return maxY;
        }

        public double maxZ() {
            return maxZ;
        }

        public void expand(AABB other) {
            final var minX = quadrupleMin(this.minX, this.maxX, other.minX, other.maxX);
            final var maxX = quadrupleMax(this.minX, this.maxX, other.minX, other.maxX);
            final var minY = quadrupleMin(this.minY, this.maxY, other.minY, other.maxY);
            final var maxY = quadrupleMax(this.minY, this.maxY, other.minY, other.maxY);
            final var minZ = quadrupleMin(this.minZ, this.maxZ, other.minZ, other.maxZ);
            final var maxZ = quadrupleMax(this.minZ, this.maxZ, other.minZ, other.maxZ);

            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        private static double quadrupleMin(double a, double b, double c, double d) {
            return Math.min(Math.min(a, b), Math.min(c, d));
        }

        private static double quadrupleMax(double a, double b, double c, double d) {
            return Math.max(Math.max(a, b), Math.max(c, d));
        }
    }
}
