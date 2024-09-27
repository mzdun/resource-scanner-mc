// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.rt.math;

import org.joml.Matrix3d;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector4d;

public class V3d implements Position {
    public static final V3d ZERO;
    public final double x;
    public final double y;
    public final double z;

    public static V3d of(V3i vec) {
        return new V3d((double) vec.getX(), (double) vec.getY(), (double) vec.getZ());
    }

    public static V3d of(Vector3d vec) {
        return new V3d(vec.x(), vec.y(), vec.z());
    }

    public static V3d add(V3i vec, double deltaX, double deltaY, double deltaZ) {
        return new V3d((double) vec.getX() + deltaX, (double) vec.getY() + deltaY, (double) vec.getZ() + deltaZ);
    }

    public static V3d ofCenter(V3i vec) {
        return add(vec, 0.5, 0.5, 0.5);
    }

    public static V3d ofBottomCenter(V3i vec) {
        return add(vec, 0.5, 0.0, 0.5);
    }

    public static V3d ofCenter(V3i vec, double deltaY) {
        return add(vec, 0.5, deltaY, 0.5);
    }

    public V3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public V3d relativize(V3d vec) {
        return new V3d(vec.x - this.x, vec.y - this.y, vec.z - this.z);
    }

    public V3d normalize() {
        double d = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        return d < 1.0E-4 ? ZERO : new V3d(this.x / d, this.y / d, this.z / d);
    }

    public double dotProduct(V3d vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z;
    }

    public V3d crossProduct(V3d vec) {
        return new V3d(this.y * vec.z - this.z * vec.y, this.z * vec.x - this.x * vec.z,
                this.x * vec.y - this.y * vec.x);
    }

    public V3d subtract(V3d vec) {
        return this.subtract(vec.x, vec.y, vec.z);
    }

    public V3d subtract(double x, double y, double z) {
        return this.add(-x, -y, -z);
    }

    public V3d add(V3d vec) {
        return this.add(vec.x, vec.y, vec.z);
    }

    public V3d add(double x, double y, double z) {
        return new V3d(this.x + x, this.y + y, this.z + z);
    }

    public boolean isInRange(Position pos, double radius) {
        return this.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < radius * radius;
    }

    public double distanceTo(V3d vec) {
        double d = vec.x - this.x;
        double e = vec.y - this.y;
        double f = vec.z - this.z;
        return Math.sqrt(d * d + e * e + f * f);
    }

    public double squaredDistanceTo(V3d vec) {
        double d = vec.x - this.x;
        double e = vec.y - this.y;
        double f = vec.z - this.z;
        return d * d + e * e + f * f;
    }

    public double squaredDistanceTo(double x, double y, double z) {
        double d = x - this.x;
        double e = y - this.y;
        double f = z - this.z;
        return d * d + e * e + f * f;
    }

    public boolean isWithinRangeOf(V3d vec, double horizontalRange, double verticalRange) {
        double d = vec.getX() - this.x;
        double e = vec.getY() - this.y;
        double f = vec.getZ() - this.z;
        return (d * d + f * f) < (horizontalRange * horizontalRange) && Math.abs(e) < verticalRange;
    }

    public V3d negate() {
        return this.multiply(-1.0);
    }

    public V3d multiply(double value) {
        return this.multiply(value, value, value);
    }

    public V3d multiply(V3d vec) {
        return this.multiply(vec.x, vec.y, vec.z);
    }

    public V3d multiply(double x, double y, double z) {
        return new V3d(this.x * x, this.y * y, this.z * z);
    }

    public V3d multiply(Matrix3d m) {
        return of(new Vector3d(x, y, z).mul(m));
    }

    public V3d multiply(Matrix4d m) {
        Vector4d v4 = new Vector4d(x, y, z, 1).mul(m);
        return new V3d(v4.x, v4.y, v4.z);
    }

    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public double lengthSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public double horizontalLength() {
        return Math.sqrt(this.x * this.x + this.z * this.z);
    }

    public double horizontalLengthSquared() {
        return this.x * this.x + this.z * this.z;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof V3d vec3d)) {
            return false;
        } else {
            if (Double.compare(vec3d.x, this.x) != 0) {
                return false;
            } else if (Double.compare(vec3d.y, this.y) != 0) {
                return false;
            } else {
                return Double.compare(vec3d.z, this.z) == 0;
            }
        }
    }

    public int hashCode() {
        long l = Double.doubleToLongBits(this.x);
        int i = (int) (l ^ l >>> 32);
        l = Double.doubleToLongBits(this.y);
        i = 31 * i + (int) (l ^ l >>> 32);
        l = Double.doubleToLongBits(this.z);
        i = 31 * i + (int) (l ^ l >>> 32);
        return i;
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    public static V3d fromPolar(float pitch, float yaw) {
        float f = Helper.cos(-yaw * 0.017453292F - 3.1415927F);
        float g = Helper.sin(-yaw * 0.017453292F - 3.1415927F);
        float h = -Helper.cos(-pitch * 0.017453292F);
        float i = Helper.sin(-pitch * 0.017453292F);
        return new V3d((double) (g * h), (double) i, (double) (f * h));
    }

    public final double getX() {
        return this.x;
    }

    public final double getY() {
        return this.y;
    }

    public final double getZ() {
        return this.z;
    }

    static {
        ZERO = new V3d(0.0, 0.0, 0.0);
    }
}
