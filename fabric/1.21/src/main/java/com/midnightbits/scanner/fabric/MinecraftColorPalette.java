// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.fabric;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.sonar.graphics.Colors;
import net.minecraft.block.MapColor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MinecraftColorPalette {
    public static final Map<Id, Colors.Proxy> BLOCK_TAG_COLORS;

    private static class MapColorProxy implements Colors.Proxy {
        MapColor target;

        MapColorProxy(MapColor target) {
            this.target = target;
        }

        @Override
        public int rgb24() {
            return MapColor.get(target.id).color;
        }


        @Override
        public boolean equals(Colors.Proxy other) {
            return (other instanceof MapColorProxy ref) && ref.target.id == target.id;
        }

        @Override
        public int compareTo(@NotNull Colors.Proxy other) {
            if (!(other instanceof MapColorProxy ref)) {
                throw new ClassCastException();
            }
            return target.id - ref.target.id;
        }
    }

    static {
        BLOCK_TAG_COLORS = new HashMap<>();
        BLOCK_TAG_COLORS.put(Id.ofVanilla("lapis_ores"), new MapColorProxy(MapColor.LAPIS_BLUE));
        BLOCK_TAG_COLORS.put(Id.ofVanilla("redstone_ores"), new MapColorProxy(MapColor.RED));
        BLOCK_TAG_COLORS.put(Id.ofVanilla("copper_ores"), new MapColorProxy(MapColor.TERRACOTTA_ORANGE));
        BLOCK_TAG_COLORS.put(Id.ofVanilla("coal_ores"), new MapColorProxy(MapColor.GRAY));
        BLOCK_TAG_COLORS.put(Id.ofVanilla("emerald_ores"), new MapColorProxy(MapColor.EMERALD_GREEN));
        BLOCK_TAG_COLORS.put(Id.ofVanilla("iron_ores"), new MapColorProxy(MapColor.BROWN));
        BLOCK_TAG_COLORS.put(Id.ofVanilla("diamond_ores"), new MapColorProxy(MapColor.DIAMOND_BLUE));
        BLOCK_TAG_COLORS.put(Id.ofVanilla("gold_ores"), new MapColorProxy(MapColor.GOLD));
    }
}
