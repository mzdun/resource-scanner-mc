// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.graphics;

import com.midnightbits.scanner.rt.animation.TickSet;
import com.midnightbits.scanner.rt.core.Services;
import com.midnightbits.scanner.sonar.Sonar;
import com.midnightbits.scanner.utils.Clock;

import java.util.function.Predicate;

public class SlicePacer implements Sonar.SlicePacer {
    public static final long DURATION = 10;

    private static class Scan {
        final Predicate<Long> callback;
        long then;
        final long delay;

        Scan(Predicate<Long> callback, long then, long delay) {
            this.callback = callback;
            this.then = then;
            this.delay = delay;
        }

        public boolean tick(long now) {
            final var duration = now - then;
            if (duration < delay) {
                return true;
            }
            then = now;
            return callback.test(now);
        }
    }

    final AbstractAnimatorHost host;
    final long delay;
    TickSet<Scan> scans = new TickSet<>();

    public SlicePacer(long delay) {
        this.host = Services.PLATFORM.getAnimatorHost();
        this.delay = delay;
    }

    @Override
    public void registerCallback(Predicate<Long> scan) {
        scans.add(new Scan(scan, Clock.currentTimeMillis(), delay));
        if (scans.size() == 1) {
            host.addAnimation(this::onTick);
        }
    }

    private boolean onTick(long now) {
        return scans.run(scan -> scan.tick(now));
    }
}
