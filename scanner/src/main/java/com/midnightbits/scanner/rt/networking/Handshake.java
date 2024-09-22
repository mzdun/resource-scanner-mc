package com.midnightbits.scanner.rt.networking;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.core.Services;

public record Handshake(String version) implements Message {
    public static final Ref<Handshake> REF = new Ref<>(Id.ofModuleV1("handshake"));

    public static Handshake of(String version) {
        return new Handshake(version);
    }

    public static Handshake reject() {
        return of(null);
    }

    public static Handshake accept() {
        return ofScannerVersion();
    }

    public static Handshake ofScannerVersion() {
        return of(Services.PLATFORM.getScannerVersion());
    }

    @Override
    public Ref<? extends Message> getRef() {
        return REF;
    }
}
