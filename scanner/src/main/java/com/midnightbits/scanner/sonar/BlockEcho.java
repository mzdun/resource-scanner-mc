// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.sonar.graphics.Colors;
import com.midnightbits.scanner.utils.Clock;
import org.jetbrains.annotations.NotNull;

public record BlockEcho(V3i position, Echo echo, long pingTime) implements Comparable<BlockEcho> {
    public record Partial(V3i position, Echo echo) {
        public static Partial of(V3i position, Echo echo) {
            return new Partial(position, echo);
        }

        public static Partial of(int x, int y, int z, Echo echo) {
            return of(new V3i(x, y, z), echo);
        }

        public Id id() {
            return echo.id();
        }

        public boolean equals(@NotNull Object obj) {
            if (!(obj instanceof Partial other)) {
                throw new ClassCastException();
            }
            return echo.equals(other.echo) && position.equals(other.position);
        }

        @Override
        public String toString() {
            return "BlockEcho.Partial.of(" +
                    position.getX() + ", " +
                    position.getY() + ", " +
                    position.getZ() + ", " +
                    echo + ")";
        }
    }

    public BlockEcho(int x, int y, int z, Echo echo, long pingTime) {
        this(new V3i(x, y, z), echo, pingTime);
    }

    public static BlockEcho echoFrom(Partial partial) {
        return new BlockEcho(partial.position, partial.echo, Clock.currentTimeMillis());
    }

    public static BlockEcho echoFrom(int x, int y, int z, Echo echo) {
        return echoFrom(Partial.of(x, y, z, echo));
    }

    public Id id() {
        return echo.id();
    }

    public Colors.Proxy color() {
        return echo.color();
    }

    public boolean equals(@NotNull Object obj) {
        if (!(obj instanceof BlockEcho other)) {
            throw new ClassCastException();
        }
        return pingTime == other.pingTime && echo.equals(other.echo) && position.equals(other.position);
    }

    @Override
    public String toString() {
        return "\nnew BlockEcho(BlockEcho.Partial.of(" +
                position.getX() + ", " +
                position.getY() + ", " +
                position.getZ() + ", " +
                echo + "), " +
                pingTime + ")";
    }

    @Override
    public int compareTo(@NotNull BlockEcho other) {
        var result = (int) (pingTime - other.pingTime);
        if (result != 0) {
            return result;
        }

        result = echo.compareTo(other.echo);
        if (result != 0)
            return result;

        return position.compareTo(other.position);
    }
}
