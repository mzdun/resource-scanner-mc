package com.midnightbits.scanner.rt.math;

public class V3i implements Comparable<V3i> {
    public static final V3i ZERO;
    private int x;
    private int y;
    private int z;

    public static V3i ofRounded(Position pos) {
        return new V3i((int) Math.round(pos.getX()), (int) Math.round(pos.getY()), (int) Math.round(pos.getZ()));
    }

    public V3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof V3i)) {
            return false;
        } else {
            V3i vec3i = (V3i) o;
            if (this.getX() != vec3i.getX()) {
                return false;
            } else if (this.getY() != vec3i.getY()) {
                return false;
            } else {
                return this.getZ() == vec3i.getZ();
            }
        }
    }

    public int hashCode() {
        return (this.getY() + this.getZ() * 31) * 31 + this.getX();
    }

    public int compareTo(V3i vec3i) {
        if (this.getY() == vec3i.getY()) {
            return this.getZ() == vec3i.getZ() ? this.getX() - vec3i.getX() : this.getZ() - vec3i.getZ();
        } else {
            return this.getY() - vec3i.getY();
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    protected V3i setX(int x) {
        this.x = x;
        return this;
    }

    protected V3i setY(int y) {
        this.y = y;
        return this;
    }

    protected V3i setZ(int z) {
        this.z = z;
        return this;
    }

    public V3i add(int x, int y, int z) {
        return x == 0 && y == 0 && z == 0 ? this : new V3i(this.getX() + x, this.getY() + y, this.getZ() + z);
    }

    public V3i add(V3i vec) {
        return this.add(vec.getX(), vec.getY(), vec.getZ());
    }

    public V3i subtract(V3i vec) {
        return this.add(-vec.getX(), -vec.getY(), -vec.getZ());
    }

    public V3i multiply(int scale) {
        if (scale == 1) {
            return this;
        } else {
            return scale == 0 ? ZERO : new V3i(this.getX() * scale, this.getY() * scale, this.getZ() * scale);
        }
    }

    public V3i crossProduct(V3i vec) {
        return new V3i(this.getY() * vec.getZ() - this.getZ() * vec.getY(),
                this.getZ() * vec.getX() - this.getX() * vec.getZ(),
                this.getX() * vec.getY() - this.getY() * vec.getX());
    }

    public boolean isWithinDistance(V3i vec, double distance) {
        return this.getSquaredDistance(vec) < (distance * distance);
    }

    public boolean isWithinDistance(Position pos, double distance) {
        return this.getSquaredDistance(pos) < (distance * distance);
    }

    public double getSquaredDistance(V3i vec) {
        return this.getSquaredDistance((double) vec.getX(), (double) vec.getY(), (double) vec.getZ());
    }

    public double getSquaredDistance(Position pos) {
        return this.getSquaredDistanceFromCenter(pos.getX(), pos.getY(), pos.getZ());
    }

    public double getSquaredDistanceFromCenter(double x, double y, double z) {
        double d = (double) this.getX() + 0.5 - x;
        double e = (double) this.getY() + 0.5 - y;
        double f = (double) this.getZ() + 0.5 - z;
        return d * d + e * e + f * f;
    }

    public double getSquaredDistance(double x, double y, double z) {
        double d = (double) this.getX() - x;
        double e = (double) this.getY() - y;
        double f = (double) this.getZ() - z;
        return d * d + e * e + f * f;
    }

    public int getManhattanDistance(V3i vec) {
        float f = (float) Math.abs(vec.getX() - this.getX());
        float g = (float) Math.abs(vec.getY() - this.getY());
        float h = (float) Math.abs(vec.getZ() - this.getZ());
        return (int) (f + g + h);
    }

    @Override
    public String toString() {
        return this.getX() + ", " + this.getY() + ", " + this.getZ();
    }

    static {
        ZERO = new V3i(0, 0, 0);
    }
}
