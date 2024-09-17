package com.midnightbits.scanner.rt.core;

import com.midnightbits.scanner.rt.text.MutableText;

public interface BlockInfo {
    boolean isAir();

    Id getId();

    MutableText getName();
}
