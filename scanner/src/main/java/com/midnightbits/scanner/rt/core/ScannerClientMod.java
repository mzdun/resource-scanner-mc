package com.midnightbits.scanner.rt.core;

import com.midnightbits.scanner.rt.networking.ClientPlayMessaging;
import com.midnightbits.scanner.sonar.BlockEcho;

public interface ScannerClientMod {
    void onInitializeClient(ClientPlayMessaging playNetworking);

    Iterable<BlockEcho> echoes();
}
