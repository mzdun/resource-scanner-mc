package com.midnightbits.scanner.utils.test;

import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.rt.math.V3d;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.test.support.Iterables;
import com.midnightbits.scanner.utils.Circle;

public class CircleTestRotation {
    private static int RADIUS = 4;
    private static int DISTANCE = 32;

    @Test
    void lookStraightOn() {
        Circle circle = new Circle(RADIUS);
        Iterables.assertEquals(new V3i[] {
                pt(-2, -4, 0), pt(-1, -4, 0), pt(0, -4, 0), pt(1, -4, 0), pt(2, -4, 0), pt(-3, -3, 0), pt(-2, -3, 0),
                pt(-1, -3, 0), pt(0, -3, 0), pt(1, -3, 0), pt(2, -3, 0), pt(3, -3, 0), pt(-4, -2, 0), pt(-3, -2, 0),
                pt(-2, -2, 0), pt(-1, -2, 0), pt(0, -2, 0), pt(1, -2, 0), pt(2, -2, 0), pt(3, -2, 0), pt(4, -2, 0),
                pt(-4, -1, 0), pt(-3, -1, 0), pt(-2, -1, 0), pt(-1, -1, 0), pt(0, -1, 0), pt(1, -1, 0), pt(2, -1, 0),
                pt(3, -1, 0), pt(4, -1, 0), pt(-4, 0, 0), pt(-3, 0, 0), pt(-2, 0, 0), pt(-1, 0, 0), pt(0, 0, 0),
                pt(1, 0, 0), pt(2, 0, 0), pt(3, 0, 0), pt(4, 0, 0), pt(-4, 1, 0), pt(-3, 1, 0), pt(-2, 1, 0),
                pt(-1, 1, 0), pt(0, 1, 0), pt(1, 1, 0), pt(2, 1, 0), pt(3, 1, 0), pt(4, 1, 0), pt(-4, 2, 0),
                pt(-3, 2, 0), pt(-2, 2, 0), pt(-1, 2, 0), pt(0, 2, 0), pt(1, 2, 0), pt(2, 2, 0), pt(3, 2, 0),
                pt(4, 2, 0), pt(-3, 3, 0), pt(-2, 3, 0), pt(-1, 3, 0), pt(0, 3, 0), pt(1, 3, 0), pt(2, 3, 0),
                pt(3, 3, 0), pt(-2, 4, 0), pt(-1, 4, 0), pt(0, 4, 0), pt(1, 4, 0), pt(2, 4, 0),
        }, circle.iterateAlongCamera(ofCamera(0, 0, DISTANCE)));
    }

    @Test
    void lookUp() {
        Circle circle = new Circle(RADIUS);
        Iterables.assertEquals(new V3i[] {
                pt(-2, 0, -4), pt(-1, 0, -4), pt(0, 0, -4), pt(1, 0, -4), pt(2, 0, -4), pt(-3, 0, -3), pt(-2, 0, -3),
                pt(-1, 0, -3), pt(0, 0, -3), pt(1, 0, -3), pt(2, 0, -3), pt(3, 0, -3), pt(-4, 0, -2), pt(-3, 0, -2),
                pt(-2, 0, -2), pt(-1, 0, -2), pt(0, 0, -2), pt(1, 0, -2), pt(2, 0, -2), pt(3, 0, -2), pt(4, 0, -2),
                pt(-4, 0, -1), pt(-3, 0, -1), pt(-2, 0, -1), pt(-1, 0, -1), pt(0, 0, -1), pt(1, 0, -1), pt(2, 0, -1),
                pt(3, 0, -1), pt(4, 0, -1), pt(-4, 0, 0), pt(-3, 0, 0), pt(-2, 0, 0), pt(-1, 0, 0), pt(0, 0, 0),
                pt(1, 0, 0), pt(2, 0, 0), pt(3, 0, 0), pt(4, 0, 0), pt(-4, 0, 1), pt(-3, 0, 1), pt(-2, 0, 1),
                pt(-1, 0, 1), pt(0, 0, 1), pt(1, 0, 1), pt(2, 0, 1), pt(3, 0, 1), pt(4, 0, 1), pt(-4, 0, 2),
                pt(-3, 0, 2), pt(-2, 0, 2), pt(-1, 0, 2), pt(0, 0, 2), pt(1, 0, 2), pt(2, 0, 2), pt(3, 0, 2),
                pt(4, 0, 2), pt(-3, 0, 3), pt(-2, 0, 3), pt(-1, 0, 3), pt(0, 0, 3), pt(1, 0, 3), pt(2, 0, 3),
                pt(3, 0, 3), pt(-2, 0, 4), pt(-1, 0, 4), pt(0, 0, 4), pt(1, 0, 4), pt(2, 0, 4),
        }, circle.iterateAlongCamera(ofCamera(90, 0, DISTANCE)));
    }

    @Test
    void lookDown() {
        Circle circle = new Circle(RADIUS);
        Iterables.assertEquals(new V3i[] {
                pt(-2, 0, 4), pt(-1, 0, 4), pt(0, 0, 4), pt(1, 0, 4), pt(2, 0, 4), pt(-3, 0, 3), pt(-2, 0, 3),
                pt(-1, 0, 3), pt(0, 0, 3), pt(1, 0, 3), pt(2, 0, 3), pt(3, 0, 3), pt(-4, 0, 2), pt(-3, 0, 2),
                pt(-2, 0, 2), pt(-1, 0, 2), pt(0, 0, 2), pt(1, 0, 2), pt(2, 0, 2), pt(3, 0, 2), pt(4, 0, 2),
                pt(-4, 0, 1), pt(-3, 0, 1), pt(-2, 0, 1), pt(-1, 0, 1), pt(0, 0, 1), pt(1, 0, 1), pt(2, 0, 1),
                pt(3, 0, 1), pt(4, 0, 1), pt(-4, 0, 0), pt(-3, 0, 0), pt(-2, 0, 0), pt(-1, 0, 0), pt(0, 0, 0),
                pt(1, 0, 0), pt(2, 0, 0), pt(3, 0, 0), pt(4, 0, 0), pt(-4, 0, -1), pt(-3, 0, -1), pt(-2, 0, -1),
                pt(-1, 0, -1), pt(0, 0, -1), pt(1, 0, -1), pt(2, 0, -1), pt(3, 0, -1), pt(4, 0, -1), pt(-4, 0, -2),
                pt(-3, 0, -2), pt(-2, 0, -2), pt(-1, 0, -2), pt(0, 0, -2), pt(1, 0, -2), pt(2, 0, -2), pt(3, 0, -2),
                pt(4, 0, -2), pt(-3, 0, -3), pt(-2, 0, -3), pt(-1, 0, -3), pt(0, 0, -3), pt(1, 0, -3), pt(2, 0, -3),
                pt(3, 0, -3), pt(-2, 0, -4), pt(-1, 0, -4), pt(0, 0, -4), pt(1, 0, -4), pt(2, 0, -4),
        }, circle.iterateAlongCamera(ofCamera(-90, 0, DISTANCE)));
    }

    @Test
    void lookBack() {
        Circle circle = new Circle(RADIUS);
        Iterables.assertEquals(new V3i[] {
                pt(2, -4, 0), pt(1, -4, 0), pt(0, -4, 0), pt(-1, -4, 0), pt(-2, -4, 0), pt(3, -3, 0), pt(2, -3, 0),
                pt(1, -3, 0), pt(0, -3, 0), pt(-1, -3, 0), pt(-2, -3, 0), pt(-3, -3, 0), pt(4, -2, 0), pt(3, -2, 0),
                pt(2, -2, 0), pt(1, -2, 0), pt(0, -2, 0), pt(-1, -2, 0), pt(-2, -2, 0), pt(-3, -2, 0), pt(-4, -2, 0),
                pt(4, -1, 0), pt(3, -1, 0), pt(2, -1, 0), pt(1, -1, 0), pt(0, -1, 0), pt(-1, -1, 0), pt(-2, -1, 0),
                pt(-3, -1, 0), pt(-4, -1, 0), pt(4, 0, 0), pt(3, 0, 0), pt(2, 0, 0), pt(1, 0, 0), pt(0, 0, 0),
                pt(-1, 0, 0), pt(-2, 0, 0), pt(-3, 0, 0), pt(-4, 0, 0), pt(4, 1, 0), pt(3, 1, 0), pt(2, 1, 0),
                pt(1, 1, 0), pt(0, 1, 0), pt(-1, 1, 0), pt(-2, 1, 0), pt(-3, 1, 0), pt(-4, 1, 0), pt(4, 2, 0),
                pt(3, 2, 0), pt(2, 2, 0), pt(1, 2, 0), pt(0, 2, 0), pt(-1, 2, 0), pt(-2, 2, 0), pt(-3, 2, 0),
                pt(-4, 2, 0), pt(3, 3, 0), pt(2, 3, 0), pt(1, 3, 0), pt(0, 3, 0), pt(-1, 3, 0), pt(-2, 3, 0),
                pt(-3, 3, 0), pt(2, 4, 0), pt(1, 4, 0), pt(0, 4, 0), pt(-1, 4, 0), pt(-2, 4, 0),
        }, circle.iterateAlongCamera(ofCamera(0, 180, DISTANCE)));
    }

    @Test
    void lookToTheSide() {
        Circle circle = new Circle(RADIUS);
        Iterables.assertEquals(new V3i[] {
                pt(3, -2, -2), pt(3, -2, -1), pt(3, -2, 0), pt(3, -2, 1), pt(3, -2, 2), pt(3, -1, -3), pt(3, -1, -2),
                pt(3, -1, -1), pt(3, -1, 0), pt(3, -1, 1), pt(3, -1, 2), pt(3, -1, 3), pt(2, -1, -4), pt(2, -1, -3),
                pt(2, -1, -2), pt(2, -1, -1), pt(2, -1, 0), pt(2, -1, 1), pt(2, -1, 2), pt(2, -1, 3), pt(2, -1, 4),
                pt(1, 0, -4), pt(1, 0, -3), pt(1, 0, -2), pt(1, 0, -1), pt(1, 0, 0), pt(1, 0, 1), pt(1, 0, 2),
                pt(1, 0, 3), pt(1, 0, 4), pt(0, 0, -4), pt(0, 0, -3), pt(0, 0, -2), pt(0, 0, -1), pt(0, 0, 0),
                pt(0, 0, 1), pt(0, 0, 2), pt(0, 0, 3), pt(0, 0, 4), pt(-1, 0, -4), pt(-1, 0, -3), pt(-1, 0, -2),
                pt(-1, 0, -1), pt(-1, 0, 0), pt(-1, 0, 1), pt(-1, 0, 2), pt(-1, 0, 3), pt(-1, 0, 4), pt(-2, 1, -4),
                pt(-2, 1, -3), pt(-2, 1, -2), pt(-2, 1, -1), pt(-2, 1, 0), pt(-2, 1, 1), pt(-2, 1, 2), pt(-2, 1, 3),
                pt(-2, 1, 4), pt(-3, 1, -3), pt(-3, 1, -2), pt(-3, 1, -1), pt(-3, 1, 0), pt(-3, 1, 1), pt(-3, 1, 2),
                pt(-3, 1, 3), pt(-3, 2, -2), pt(-3, 2, -1), pt(-3, 2, 0), pt(-3, 2, 1), pt(-3, 2, 2),
        }, circle.iterateAlongCamera(ofCamera(60, 90, DISTANCE)));
    }

    static V3i pt(int x, int y, int z) {
        return new V3i(x, y, z);
    }

    @SuppressWarnings("unused")
    private void assertEquals_(V3i[] expectedRaw, Iterable<V3i> circle) {
        StringBuilder builder = new StringBuilder();
        for (V3i pos : circle) {
            builder.append(String.format(" pt(%s),", pos));
        }
        builder.append('\n');
        System.out.println(builder);
    }

    private static V3i ofCamera(float pitch, float yaw, int distance) {
        return V3i.ofRounded(V3d.fromPolar(pitch, yaw).multiply(distance));
    }
}
