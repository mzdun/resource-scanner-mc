package com.midnightbits.scanner.utils.test;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.midnightbits.scanner.rt.math.V3d;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.utils.Circle;
import com.midnightbits.scanner.utils.Circle.PitchAndYaw;

public class CircleMatrixTest {
    private static final int DISTANCE = 32;
    private static final Logger LOGGER = LoggerFactory.getLogger("MatrixTest");

    private record Rotate(float pitch, float yaw, PitchAndYaw expected) {
        public static Rotate of(float pitchF, float yawF, double pitchD, double yawD) {
            return new Rotate(pitchF, yawF, new PitchAndYaw(pitchD, yawD));
        }

        public static Rotate of(float pitch, float yaw) {
            return of(pitch, yaw, pitch * Math.PI / 180, yaw * Math.PI / 180);
        }
    };

    @ParameterizedTest
    @MethodSource("provideTests")
    void retrievePitchAndYaw(Rotate args) {
        final var cam = ofCamera(args.pitch(), args.yaw());
        final var actual = Circle.getPitchAndYaw(cam);
        final var pitchDiff = Math.abs(actual.pitch() - args.expected.pitch());
        final var yawDiff = Math.abs(actual.yaw() - args.expected.yaw());
        if (pitchDiff > .02 || yawDiff > .005) {
            LOGGER.debug("expected:({}, {}), actual:({}, {}), diff:({}, {}), cam:({})",
                    args.expected.pitch(), args.expected.yaw(), actual.pitch(), actual.yaw(), pitchDiff, yawDiff, cam);
        }
        Assertions.assertTrue(pitchDiff < .02);
        Assertions.assertTrue(yawDiff < .005);
    }

    @ParameterizedTest
    @MethodSource("provideTests")
    void rotatePitchAndYaw(Rotate args) {
        final var expected = ofCamera(args.pitch(), args.yaw());
        final var rot = Circle.rotatePitchYaw(expected);
        final var actual = V3i.ofRounded(V3d.of(new V3i(0, 0, DISTANCE)).multiply(rot));

        final var diff = expected.subtract(actual);

        if (diff.getX() != 0 || diff.getY() != 0 || diff.getZ() != 0) {
            LOGGER.debug("expected:{}; actual:{}; diff:{}", expected, actual, diff);
        }

        Assertions.assertEquals(expected, actual);
    }

    private static Stream<Arguments> provideTests() {
        return Stream.of(
                Arguments.of(Rotate.of(0, 0)),
                Arguments.of(Rotate.of(60, 0)),
                Arguments.of(Rotate.of(90, 0)),
                Arguments.of(Rotate.of(90, 45, Math.PI / 2, 0)),
                Arguments.of(Rotate.of(-60, 0)),
                Arguments.of(Rotate.of(-90, 0)),
                Arguments.of(Rotate.of(0, 45)),
                Arguments.of(Rotate.of(0, 60)),
                Arguments.of(Rotate.of(0, 90)),
                Arguments.of(Rotate.of(0, 120)),
                Arguments.of(Rotate.of(0, 150)),
                Arguments.of(Rotate.of(0, 180, 0, -Math.PI)),
                Arguments.of(Rotate.of(0, -45)),
                Arguments.of(Rotate.of(0, -60)),
                Arguments.of(Rotate.of(0, -90)),
                Arguments.of(Rotate.of(0, -120)),
                Arguments.of(Rotate.of(0, -150)),
                Arguments.of(Rotate.of(0, -180)),
                Arguments.of(Rotate.of(60, 45)),
                Arguments.of(Rotate.of(60, 60)),
                Arguments.of(Rotate.of(-60, -45)),
                Arguments.of(Rotate.of(-60, 45)),
                Arguments.of(Rotate.of(60, -45)));
    }

    private static V3i ofCamera(float pitch, float yaw) {
        return V3i.ofRounded(V3d.fromPolar(pitch, yaw).multiply(CircleMatrixTest.DISTANCE));
    }
}
