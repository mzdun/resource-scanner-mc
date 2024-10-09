// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.graphics;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.core.ScannerMod;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.sonar.BlockEcho;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoState {
    private static final String TAG = ScannerMod.MOD_ID;
    private static final Logger LOGGER = LoggerFactory.getLogger(TAG);
    public final BlockEcho echo;
    public int sides = Pixel.ALL_SIDES;
    public int edges = 0;
    public int alpha = Colors.ECHO_ALPHA;

    public EchoState(BlockEcho echo) {
        this.echo = echo;
    }

    public V3i position() {
        return echo.position();
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
                LOGGER.debug("[{}]: skipping: {}/{}", echo.position(), side, Pixel.side_names[side]);
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
                LOGGER.debug("[{}]: skipping: {}/{}", echo.position(), index, Pixel.edge_names[index]);
                continue;
            }

            Pixel.edges[index].apply(buffer, m, validColor);
        }
        LOGGER.debug("");

        matrices.pop();
    }

    public AABB getBounds() {
        final var pos = echo.position();
        double minX = pos.getX();
        double minY = pos.getY();
        double minZ = pos.getZ();

        return new AABB(minX, minY, minZ, minX + 1, minY + 1, minZ + 1);
    }

    private Matrix4f pushAndAlignWithCamera(MatrixStack matrices, Vector3f camera) {
        final var x = position().getX() - camera.x;
        final var y = position().getY() - camera.y;
        final var z = position().getZ() - camera.z;
        matrices.push();
        matrices.translate(x, y, z);
        return matrices.peek().getPositionMatrix();
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

        public double minX() { return minX; }
        public double minY() { return minY; }
        public double minZ() { return minZ; }
        public double maxX() { return maxX; }
        public double maxY() { return maxY; }
        public double maxZ() { return maxZ; }

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
