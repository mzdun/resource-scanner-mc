package com.midnightbits.scanner.rt.core;

import com.midnightbits.scanner.rt.networking.ServerPlayMessaging;

public interface ScannerServerMod {
    void onInitializeServer(ServerPlayMessaging playNetworking);
}
