package com.midnightbits.scanner.utils.test;

import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.utils.ConeOfBlocks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class ConeSlicerTest {
    static final Logger LOGGER = LoggerFactory.getLogger("ConeSlicerTest");

    @Test
    void simplestConeTest() {
        LOGGER.info("simplestConeTest:");
        final var cone = ConeOfBlocks.fromCamera(new V3i(0, 0, 0), 0F, 0F, 4, 0);
        for (final var slice : cone.sliced()) {
            final var trueDist = ((slice.distance() * 1000 + 5) / ConeOfBlocks.Slicer.PRECISION) / 10;
            LOGGER.warn(">>> slice@{}.{} [{}]: {} item(s): ([{}])",
                    trueDist/100, String.format("%02d", trueDist%100), slice.distance(),
                    slice.items().size(),
                    slice.items().stream().map(String::valueOf).collect(Collectors.joining("], [")));
        }

        final var slicer = cone.sliced();

        int expectedDistance = 0;
        for (final var slice : slicer) {
            Assertions.assertEquals(expectedDistance, slice.distance() / ConeOfBlocks.Slicer.PRECISION);
            Assertions.assertEquals(1, slice.items().size());
            expectedDistance++;
        }
    }

    protected record Expectation(int distPercentile, int size) {
    }

    @Test
    void simplestConeTestRadius2() {
        LOGGER.info("simplestConeTestRadius2:");
        final var cone = ConeOfBlocks.fromCamera(new V3i(0, 0, 0), 0F, 0F, 2, 2);
        final var slicer = cone.sliced();

        final var expectations = new Expectation[] {
                new Expectation(0, 1),
                new Expectation(100, 1),
                new Expectation(150, 4),
                new Expectation(175, 4),
                new Expectation(200, 1),
                new Expectation(225, 4),
                new Expectation(250, 4),
                new Expectation(275, 4),
                new Expectation(300, 8),
                new Expectation(350, 4),
        };

        int index = 0;
        for (final var slice : slicer) {
            Assertions.assertTrue(index < expectations.length);
            final var expectation = expectations[index];

            Assertions.assertEquals(expectation.distPercentile(),
                    ((slice.distance() * 1000 + 5) / ConeOfBlocks.Slicer.PRECISION) / 10);
            Assertions.assertEquals(expectation.size(), slice.items().size());
            index++;
        }
    }
}
