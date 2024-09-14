package com.midnightbits.scanner.rt.core;

import com.midnightbits.scanner.rt.text.MutableText;

public interface BlockInfo {
    public boolean isAir();

    public Id getId();

    public MutableText getName();
}
