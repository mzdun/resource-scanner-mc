package com.midnightbits.scanner.rt.event.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.rt.event.Event;
import com.midnightbits.scanner.rt.event.EventEmitterOf;
import com.midnightbits.scanner.rt.event.EventListener;
import com.midnightbits.scanner.test.support.Counter;

public class EventEmitterTest {
    private final static class TestEvent extends Event {
        public TestEvent(boolean cancelable) {
            super(cancelable);
        }
    };

    private final static class Emitter extends EventEmitterOf.Impl<TestEvent> {
        public void dispatch(boolean cancelable) {
            dispatchEvent(new TestEvent(cancelable));
        }

        public void dispatchNull() {
            dispatchEvent(null);
        }
    }

    @Test
    void cancellableWorks() {
        final var emitter = new Emitter();
        final var counterA = new Counter();
        final var counterB = new Counter();
        final EventListener<TestEvent> handlerA = (e) -> {
            counterA.inc();
            e.cancel();
        };
        final EventListener<TestEvent> handlerB = (e) -> counterB.inc();

        emitter.removeEventListener(handlerA);
        emitter.removeEventListener(handlerB);

        Assertions.assertThrows(AssertionError.class, () -> emitter.dispatchNull());
        Assertions.assertEquals(0, counterA.get());
        Assertions.assertEquals(0, counterB.get());

        emitter.dispatch(true);
        Assertions.assertEquals(0, counterA.get());
        Assertions.assertEquals(0, counterB.get());

        emitter.addEventListener(handlerA);
        emitter.addEventListener(handlerB);

        emitter.dispatch(true);
        Assertions.assertEquals(1, counterA.get());
        Assertions.assertEquals(0, counterB.get());

        emitter.dispatch(false);
        Assertions.assertEquals(2, counterA.get());
        Assertions.assertEquals(1, counterB.get());

        emitter.removeEventListener(handlerA);

        emitter.dispatch(true);
        Assertions.assertEquals(2, counterA.get());
        Assertions.assertEquals(2, counterB.get());
    }
}
