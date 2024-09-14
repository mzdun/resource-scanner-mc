package com.midnightbits.scanner.rt.text;

public interface MutableText extends Text {
    MutableText append(Text chunk);

    MutableText append(String literal);

    MutableText formattedGold();
}
