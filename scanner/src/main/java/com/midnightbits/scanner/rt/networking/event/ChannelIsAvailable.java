package com.midnightbits.scanner.rt.networking.event;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.event.Event;

public class ChannelIsAvailable extends Event {
    final Id id;

    public ChannelIsAvailable(Id id) {
        super();
        this.id = id;
    }

    public Id id() {
        return id;
    }
}
