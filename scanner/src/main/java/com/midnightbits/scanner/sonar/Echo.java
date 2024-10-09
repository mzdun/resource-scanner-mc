// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.sonar.graphics.Colors;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record Echo(Id id, Colors.Proxy color) implements Comparable<Echo> {

    public static Echo of(Id id, Colors.Proxy color) {
        return new Echo(id, color);
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (!(obj instanceof Echo other)) {
            throw new ClassCastException();
        }
        if (this == other) {
            return true;
        }
        return id.equals(other.id) && color.equals(other.color);
    }

    @Override
    public String toString() {
        return "new Echo(" + _idOf(id) + ", " + _colorOf(color) + ")";
    }

    private static String _idOf(Id id) {
        final var builder = new StringBuilder();
        builder.append("Id.of");
        if (Objects.equals(id.getNamespace(), Id.DEFAULT_NAMESPACE)) {
            builder.append("Vanilla(\"");
        } else {
            builder.append("(\"").append(id.getNamespace()).append("\", \"");
        }
        builder.append(id.getPath()).append("\")");
        return builder.toString();
    }

    private static String _colorOf(Colors.Proxy color) {
        for (final var entry : Colors.BLOCK_TAG_COLORS.entrySet()) {
            if (color.equals(entry.getValue())) {
                return "Colors.BLOCK_TAG_COLORS.get(" + _idOf(entry.getKey()) + ")";
            }
        }

        return _rgbOf(color.rgb24());
    }

    private static String _rgbOf(int rgb24) {
        if (rgb24 == Colors.VANILLA) {
            return "new Colors.DirectValue(Colors.VANILLA)";
        }
        if (rgb24 == Colors.PURPLE) {
            return "new Colors.DirectValue(Colors.PURPLE)";
        }
        return String.format("new Colors.DirectValue(0x%06X)", rgb24);
    }

    @Override
    public int compareTo(@NotNull Echo other) {
        if (this == other) {
            return 0;
        }

        final int result = id.compareTo(other.id);
        if (result != 0)
            return result;

        return color.compareTo(other.color);
    }
}
