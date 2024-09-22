package com.midnightbits.scanner.rt.networking;

import com.midnightbits.scanner.rt.core.Id;

public interface Message {
    public record Ref<T extends Message>(Id id) {
    }

    Ref<? extends Message> getRef();
}
