package com.midnightbits.scanner.rt.animation.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.rt.animation.Animation;
import com.midnightbits.scanner.test.support.Counter;

public class SequenceAnimation {
    @Test
    void sequenceCanBeRanAlong() {
        final var counter = new Counter();
        final var anim = Animation.from((now) -> {
            counter.inc();
            return false;
        }).andThen((now) -> {
            counter.inc();
            return false;
        });
        Assertions.assertTrue(anim instanceof Animation.SequenceAnimation);
        final var seq = (Animation.SequenceAnimation) anim;

        Assertions.assertEquals(0, counter.get());
        Assertions.assertTrue(seq.apply(0));
        Assertions.assertEquals(1, counter.get());
        Assertions.assertFalse(seq.apply(0));
        Assertions.assertEquals(2, counter.get());
        Assertions.assertFalse(seq.apply(0));
        Assertions.assertEquals(2, counter.get());
    }
}
