// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.fabric;

import com.midnightbits.scanner.rt.text.MutableText;
import com.midnightbits.scanner.rt.text.TextSupportInterface;

public class FabricTextSupportInterface implements TextSupportInterface {
    @Override
    public MutableText literal(String string) {
        return new FabricMutableText(net.minecraft.text.Text.literal(string));
    }
}
