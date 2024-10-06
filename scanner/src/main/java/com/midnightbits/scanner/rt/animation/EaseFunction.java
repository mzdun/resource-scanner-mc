// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.rt.animation;

/**
 * Specifies a cubic Bézier easing function. The four numbers specify points P1
 * and P2 of the curve
 * as (x1, y1, x2, y2). Both x values must be in the range [0, 1] or the
 * definition is invalid.
 *
 * @see <a href=
 *      "https://www.w3.org/TR/css-easing-2/#cubic-bezier-easing-functions">Cubic
 *      Bézier easing functions</a> in CSS specification
 */
public record EaseFunction(double x1, double y1, double x2, double y2) implements TimeFunction {

    public static final EaseFunction EASE = new EaseFunction(0.25F, 0.1F, 0.25F, 1F);
    public static final EaseFunction EASE_IN = new EaseFunction(0.42F, 0F, 1F, 1F);
    public static final EaseFunction EASE_OUT = new EaseFunction(0F, 0F, 0.58F, 1F);
    public static final EaseFunction EASE_IN_OUT = new EaseFunction(0.42F, 0.1F, 0.58F, 1F);

    @Override
    public double apply(double x) {
        return x;
    }
}
