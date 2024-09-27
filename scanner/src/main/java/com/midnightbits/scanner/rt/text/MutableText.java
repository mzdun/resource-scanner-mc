// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.rt.text;

public interface MutableText extends Text {
    MutableText append(Text chunk);

    MutableText append(String literal);

    MutableText formattedGold();
}
