// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils;

import java.util.Iterator;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4d;

import com.midnightbits.scanner.rt.math.V3d;
import com.midnightbits.scanner.rt.math.V3i;

public final class Circle implements Iterable<V3i> {
    public static final int RADIUS = 10;
    private final int radius;

    public Circle() {
        this(RADIUS);
    }

    public Circle(int radius) {
        this.radius = radius;
    }

    public int getRadius() {
        return radius;
    }

    public record PitchAndYaw(double pitch, double yaw) {
    };

    private static int sign(int value) {
        return value < 0 ? -1 : 1;
    }

    public static PitchAndYaw getPitchAndYaw(V3i camera) {
        if (camera.getX() == 0 && camera.getZ() == 0) {
            if (camera.getY() < 0)
                return new PitchAndYaw(Math.PI / 2, 0);
            return new PitchAndYaw(-Math.PI / 2, 0);
        }
        final double x = camera.getX();
        final double y = camera.getY();
        final double z = camera.getZ();

        final double xzLenSquared = x * x + z * z;

        final double xzLen = Math.sqrt(xzLenSquared);
        final double len = Math.sqrt(xzLenSquared + y * y);

        final double minusYaw = Math.asin(x / xzLen);
        final double firstQYaw = (Math.abs(minusYaw) < 2 * Double.MIN_VALUE) ? 0.0 : -minusYaw;
        final double yaw = camera.getZ() < 0 ? (-sign(camera.getX()) * Math.PI - firstQYaw) : firstQYaw;

        final double minusPitch = Math.asin(y / len);
        final double pitch = (Math.abs(minusPitch) < 2 * Double.MIN_VALUE) ? 0.0 : -minusPitch;

        return new PitchAndYaw(pitch, yaw);
    }

    public static Matrix4d rotatePitchYaw(V3i camera) {
        PitchAndYaw vector = getPitchAndYaw(camera);
        return new Matrix4d().rotateY(-vector.yaw()).rotateX(vector.pitch()).determineProperties();
    }

    @NotNull
    @Override
    public Iterator<V3i> iterator() {
        return this.new PixelIterator();
    }

    public Iterable<V3i> iterateAlongCamera(V3i camera) {
        Matrix4d m = rotatePitchYaw(camera);
        if ((m.properties() & Matrix4d.PROPERTY_IDENTITY) != 0) {
            return this;
        }
        return this.new MovedIterable(m);
    }

    private class MovedIterable implements Iterable<V3i> {
        Matrix4d rotation;

        MovedIterable(Matrix4d rotation) {
            this.rotation = rotation;
        }

        @NotNull
        @Override
        public Iterator<V3i> iterator() {
            return this.new OuterIterator();
        }

        private class OuterIterator implements Iterator<V3i> {
            final Iterator<V3i> inner = Circle.this.iterator();

            @Override
            public boolean hasNext() {
                return inner.hasNext();
            }

            @Override
            public V3i next() {
                V3i plain = inner.next();
                return V3i.ofRounded(V3d.of(plain).multiply(rotation));
            }
        };
    };

    private class PixelIterator implements Iterator<V3i> {
        final V3i center = V3i.ZERO;
        final double RSquared = (double) (radius + 1) * (double) (radius + 1);

        int x = -radius;
        int y = -radius;

        V3i nextItem;

        public PixelIterator() {
            this.nextItem = nextPixel();
        }

        private V3i nextPixel() {
            while (y <= radius) {
                V3i result = new V3i(x, y, 0);
                ++x;
                if (x > radius) {
                    x = -radius;
                    ++y;
                }
                double DSquared = center.getSquaredDistance(result);
                if (DSquared < RSquared)
                    return result;
            }
            return null;
        }

        @Override
        public boolean hasNext() {
            return nextItem != null;
        }

        @Override
        public V3i next() {
            V3i result = nextItem;
            nextItem = nextPixel();
            return result;
        }
    }
}
