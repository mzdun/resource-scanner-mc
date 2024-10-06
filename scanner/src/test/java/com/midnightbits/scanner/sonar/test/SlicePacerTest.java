package com.midnightbits.scanner.sonar.test;

import java.util.function.Predicate;

import com.midnightbits.scanner.rt.core.Services;
import com.midnightbits.scanner.test.mocks.platform.MockAnimatorHost;
import com.midnightbits.scanner.test.mocks.platform.MockPlatform;

import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.sonar.graphics.SlicePacer;
import com.midnightbits.scanner.test.mocks.MockedClock;
import com.midnightbits.scanner.test.support.Counter;

public class SlicePacerTest {
    final MockedClock clock = new MockedClock();

    @Test
    void addMultipleScans() {
        ((MockPlatform) Services.PLATFORM).setHostBackend((shimmers) -> {
        });
        final var ticker = (MockAnimatorHost) Services.PLATFORM.getAnimatorHost();
        final var tested = new SlicePacer(SlicePacer.DURATION);
        final var counter = new Counter();

        appendScanProcess(tested, 0x65432, counter);
        appendScanProcess(tested, 0x65462, counter);
        appendScanProcess(tested, 0x65492, counter);

        ticker.runAll(clock);
    }

    void appendScanProcess(SlicePacer tested, long time, Counter counter) {
        clock.timeStamp = time;
        final var target = time + 10 * SlicePacer.DURATION;
        Predicate<Long> scan = (now) -> {
            counter.inc();
            return now <= target;
        };
        tested.registerCallback(scan);
    }
}
