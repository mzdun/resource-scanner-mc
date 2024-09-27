// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.rt.core.fabric;

import com.midnightbits.scanner.rt.text.MutableText;
import com.midnightbits.scanner.rt.text.Text;
import com.midnightbits.scanner.fabric.FabricMutableText;
import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.math.V3i;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;

public interface Minecraft {
    static V3i v3iOf(Vec3i pos) {
        return new V3i(pos.getX(), pos.getY(), pos.getZ());
    }

    static Vec3i vec3iOf(V3i pos) {
        return new Vec3i(pos.getX(), pos.getY(), pos.getZ());
    }

    static Id idOf(Identifier id) {
        return Id.of(id.getNamespace(), id.getPath());
    }

    static Identifier identifierOf(Id id) {
        return Identifier.of(id.getNamespace(), id.getPath());
    }

    static MutableText packText(net.minecraft.text.MutableText text) {
        return new FabricMutableText(text);
    }

    static net.minecraft.text.Text unpackText(Text text) {
        return ((FabricMutableText) text).unpack();
    }
}
