// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.rt.animation;

public interface TimeFunction {
    /**
     * This method should return corresponding rate of change of some external
     * value, such as if animating from ORIGINAL to FINAL, at any given time
     * `x`, the current value of a property should be
     *
     * <pre>
     *    ORIGINAL + (FINAL - ORIGINAL)*fn.apply(`x`)
     * </pre>
     *
     * @param x time in range [0.0F, 1.0F]
     * @return the expected rate of change
     */
    double apply(double x);

    default AnimationDeclaration lastingFor(long duration) {
        return new AnimationDeclaration(duration, this);
    }

    LinearFunction LINEAR = new LinearFunction();

    class LinearFunction implements TimeFunction {
        private LinearFunction() {
        }

        @Override
        public double apply(double x) {
            return x;
        }
    }
}
