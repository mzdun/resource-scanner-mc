package com.midnightbits.scanner.rt.math;

import java.util.function.Consumer;

public class Helper {
    public static <T> T make(T object, Consumer<? super T> initializer) {
        initializer.accept(object);
        return object;
    }

    private static final float[] SINE_TABLE = (float[]) make(new float[65536], (sineTable) -> {
        for (int i = 0; i < sineTable.length; ++i) {
            sineTable[i] = (float) Math.sin((double) i * Math.PI * 2.0 / 65536.0);
        }
    });

    public static float sin(float value) {
        return SINE_TABLE[(int) (value * 10430.378F) & '\uffff'];
    }

    public static float cos(float value) {
        return SINE_TABLE[(int) (value * 10430.378F + 16384.0F) & '\uffff'];
    }
}
