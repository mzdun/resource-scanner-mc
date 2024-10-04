package com.midnightbits.scanner.rt.animation;

public class ReverseFunction implements TimeFunction {
    private final TimeFunction inner;

    public static ReverseFunction of(TimeFunction inner) {
        return new ReverseFunction(inner);
    }

    private ReverseFunction(TimeFunction inner) {
        this.inner = inner;
    }

    @Override
    public double apply(double x) {
        return inner.apply(1.0 - x);
    }
}
