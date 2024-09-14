package com.midnightbits.scanner.utils.test;

import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.test.support.Iterables;
import com.midnightbits.scanner.utils.LineOfBlocks;

public class LineOfBlocksTest {
    @Test
    void moveAlongXAxis() {
        V3i origin = new V3i(15, 20, 40);
        test(new V3i[] {
                origin,
                origin.add(1, 0, 0),
                origin.add(2, 0, 0),
                origin.add(3, 0, 0),
                origin.add(4, 0, 0),
                origin.add(5, 0, 0),
                origin.add(6, 0, 0),
                origin.add(7, 0, 0),
                origin.add(8, 0, 0),
                origin.add(9, 0, 0),
                origin.add(10, 0, 0),
        }, origin, origin.add(10, 0, 0));
    }

    @Test
    void moveAlongXAxisReversed() {
        V3i origin = new V3i(15, 20, 40);
        test(new V3i[] {
                origin,
                origin.add(-1, 0, 0),
                origin.add(-2, 0, 0),
                origin.add(-3, 0, 0),
                origin.add(-4, 0, 0),
                origin.add(-5, 0, 0),
                origin.add(-6, 0, 0),
                origin.add(-7, 0, 0),
                origin.add(-8, 0, 0),
                origin.add(-9, 0, 0),
                origin.add(-10, 0, 0),
        }, origin, origin.add(-10, 0, 0));
    }

    @Test
    void moveAlongYAxis() {
        V3i origin = new V3i(15, 20, 40);
        test(new V3i[] {
                origin,
                origin.add(0, 1, 0),
                origin.add(0, 2, 0),
                origin.add(0, 3, 0),
                origin.add(0, 4, 0),
                origin.add(0, 5, 0),
                origin.add(0, 6, 0),
                origin.add(0, 7, 0),
                origin.add(0, 8, 0),
                origin.add(0, 9, 0),
                origin.add(0, 10, 0),
        }, origin, origin.add(0, 10, 0));
    }

    @Test
    void moveAlongYAxisReversed() {
        V3i origin = new V3i(15, 20, 40);
        test(new V3i[] {
                origin,
                origin.add(0, -1, 0),
                origin.add(0, -2, 0),
                origin.add(0, -3, 0),
                origin.add(0, -4, 0),
                origin.add(0, -5, 0),
                origin.add(0, -6, 0),
                origin.add(0, -7, 0),
                origin.add(0, -8, 0),
                origin.add(0, -9, 0),
                origin.add(0, -10, 0),
        }, origin, origin.add(0, -10, 0));
    }

    @Test
    void moveAlongZAxis() {
        V3i origin = new V3i(15, 20, 40);
        test(new V3i[] {
                origin,
                origin.add(0, 0, 1),
                origin.add(0, 0, 2),
                origin.add(0, 0, 3),
                origin.add(0, 0, 4),
                origin.add(0, 0, 5),
                origin.add(0, 0, 6),
                origin.add(0, 0, 7),
                origin.add(0, 0, 8),
                origin.add(0, 0, 9),
                origin.add(0, 0, 10),
        }, origin, origin.add(0, 0, 10));
    }

    @Test
    void moveAlongZAxisReversed() {
        V3i origin = new V3i(15, 20, 40);
        test(new V3i[] {
                origin,
                origin.add(0, 0, -1),
                origin.add(0, 0, -2),
                origin.add(0, 0, -3),
                origin.add(0, 0, -4),
                origin.add(0, 0, -5),
                origin.add(0, 0, -6),
                origin.add(0, 0, -7),
                origin.add(0, 0, -8),
                origin.add(0, 0, -9),
                origin.add(0, 0, -10),
        }, origin, origin.add(0, 0, -10));
    }

    @Test
    void moveAlongXYAxes() {
        V3i origin = new V3i(15, 20, 40);
        test(new V3i[] {
                new V3i(15, 20, 40),
                new V3i(15, 21, 40),
                new V3i(16, 22, 40),
                new V3i(16, 23, 40),
                new V3i(16, 24, 40),
                new V3i(17, 25, 40),
                new V3i(17, 26, 40),
                new V3i(17, 27, 40),
                new V3i(18, 28, 40),
                new V3i(18, 29, 40),
                new V3i(18, 30, 40),
                new V3i(19, 31, 40),
                new V3i(19, 32, 40),
                new V3i(19, 33, 40),
                new V3i(20, 34, 40),
                new V3i(20, 35, 40),
                new V3i(20, 36, 40),
                new V3i(21, 37, 40),
                new V3i(21, 38, 40),
                new V3i(21, 39, 40),
                new V3i(22, 40, 40),
                new V3i(22, 41, 40),
                new V3i(22, 42, 40),
                new V3i(23, 43, 40),
                new V3i(23, 44, 40),
                new V3i(23, 45, 40),
                new V3i(24, 46, 40),
                new V3i(24, 47, 40),
                new V3i(24, 48, 40),
                new V3i(25, 49, 40),
                new V3i(25, 50, 40),
        }, origin, origin.add(10, 30, 0));
    }

    @Test
    void moveAlongAllAxes() {
        V3i origin = new V3i(15, 20, 40);
        test(new V3i[] {
                new V3i(15, 20, 40),
                new V3i(15, 19, 40),
                new V3i(16, 18, 40),
                new V3i(16, 17, 40),
                new V3i(16, 16, 41),
                new V3i(17, 15, 41),
                new V3i(17, 14, 41),
                new V3i(17, 13, 41),
                new V3i(18, 12, 41),
                new V3i(18, 11, 41),
                new V3i(18, 10, 41),
                new V3i(19, 9, 41),
                new V3i(19, 8, 42),
                new V3i(19, 7, 42),
                new V3i(20, 6, 42),
                new V3i(20, 5, 42),
                new V3i(20, 4, 42),
                new V3i(21, 3, 42),
                new V3i(21, 2, 42),
                new V3i(21, 1, 43),
                new V3i(22, 0, 43),
                new V3i(22, -1, 43),
                new V3i(22, -2, 43),
                new V3i(23, -3, 43),
                new V3i(23, -4, 43),
                new V3i(23, -5, 43),
                new V3i(24, -6, 43),
                new V3i(24, -7, 44),
                new V3i(24, -8, 44),
                new V3i(25, -9, 44),
                new V3i(25, -10, 44),
        }, origin, origin.add(10, -30, 4));
    }

    @Test
    void moveAlongAllAxesXThenZ() {
        V3i origin = new V3i(15, 20, 40);
        test(new V3i[] {
                new V3i(15, 20, 40),
                new V3i(16, 20, 41),
                new V3i(16, 20, 42),
                new V3i(17, 21, 43),
                new V3i(18, 21, 44),
                new V3i(19, 21, 45),
                new V3i(19, 21, 46),
                new V3i(20, 22, 47),
                new V3i(21, 22, 48),
                new V3i(21, 22, 49),
                new V3i(22, 22, 50),
                new V3i(23, 22, 51),
                new V3i(24, 23, 52),
                new V3i(24, 23, 53),
                new V3i(25, 23, 54)
        }, origin, origin.add(10, 3, 14));
    }

    @Test
    void brokenRange() {
        V3i origin = new V3i(15, 20, 40);
        test(new V3i[] { origin }, origin, origin);
    }

    @Test
    void polarRange_0_0() {
        Iterables.assertEquals(new V3i[] {
                new V3i(0, 0, 0),
                new V3i(0, 0, 1),
                new V3i(0, 0, 2),
                new V3i(0, 0, 3),
                new V3i(0, 0, 4),
                new V3i(0, 0, 5),
                new V3i(0, 0, 6),
                new V3i(0, 0, 7),
                new V3i(0, 0, 8),
                new V3i(0, 0, 9),
                new V3i(0, 0, 10)
        }, LineOfBlocks.fromCamera(V3i.ZERO, 0f, 0f, 10).iterate());
    }

    @Test
    void polarRange_90_0() {
        Iterables.assertEquals(new V3i[] {
                new V3i(0, 0, 0),
                new V3i(0, -1, 0),
                new V3i(0, -2, 0),
                new V3i(0, -3, 0),
                new V3i(0, -4, 0),
                new V3i(0, -5, 0),
                new V3i(0, -6, 0),
                new V3i(0, -7, 0),
                new V3i(0, -8, 0),
                new V3i(0, -9, 0),
                new V3i(0, -10, 0)
        }, LineOfBlocks.fromCamera(V3i.ZERO, 90f, 0f, 10).iterate());
    }

    @Test
    void polarRange_0_90() {
        Iterables.assertEquals(new V3i[] {
                new V3i(-0, 0, 0),
                new V3i(-1, 0, 0),
                new V3i(-2, 0, 0),
                new V3i(-3, 0, 0),
                new V3i(-4, 0, 0),
                new V3i(-5, 0, 0),
                new V3i(-6, 0, 0),
                new V3i(-7, 0, 0),
                new V3i(-8, 0, 0),
                new V3i(-9, 0, 0),
                new V3i(-10, 0, 0)
        }, LineOfBlocks.fromCamera(V3i.ZERO, 0f, 90f, 10).iterate());
    }

    private void test(V3i[] expected, V3i from, V3i to) {
        Iterables.assertEquals(expected, new LineOfBlocks(from, to).iterate());
    }
}
