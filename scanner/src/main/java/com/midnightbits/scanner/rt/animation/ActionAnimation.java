// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.rt.animation;

import com.midnightbits.scanner.utils.NotificationConsumer;

import java.util.function.Consumer;

public class ActionAnimation implements Animation {
    private final Consumer<Long> consumer;

    public ActionAnimation(Consumer<Long> consumer) {
        this.consumer = consumer;
    }

    public ActionAnimation(NotificationConsumer notificationConsumer) {
        this((now) -> notificationConsumer.apply());
    }

    @Override
    public boolean apply(long now) {
        consumer.accept(now);
        return false;
    }
}
