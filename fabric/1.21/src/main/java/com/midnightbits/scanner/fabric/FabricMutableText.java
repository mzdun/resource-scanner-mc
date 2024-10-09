// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.fabric;

import com.midnightbits.scanner.rt.text.MutableText;
import com.midnightbits.scanner.rt.text.Text;

import net.minecraft.util.Formatting;

public class FabricMutableText implements MutableText {

    net.minecraft.text.MutableText text;

    public FabricMutableText(net.minecraft.text.MutableText text) {
        this.text = text;
    }

    public net.minecraft.text.MutableText unpack() {
        return text;
    }

    @Override
    public MutableText append(Text chunk) {
        text.append(((FabricMutableText) chunk).unpack());
        return this;
    }

    @Override
    public MutableText append(String literal) {
        text.append(literal);
        return this;
    }

    @Override
    public MutableText formattedGold() {
        text.formatted(Formatting.GOLD);
        return this;
    }

    @Override
    public String getString() {
        return text.getString();
    }
}
