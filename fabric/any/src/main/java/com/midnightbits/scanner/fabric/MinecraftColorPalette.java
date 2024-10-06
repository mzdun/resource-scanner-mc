// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.fabric;

import com.midnightbits.scanner.rt.core.Id;
import net.minecraft.block.MapColor;

import java.util.HashMap;
import java.util.Map;

public class MinecraftColorPalette {
    public static final Map<Id, Integer> BLOCK_TAG_COLORS;

    static {
        BLOCK_TAG_COLORS = new HashMap<>();
        BLOCK_TAG_COLORS.put(Id.ofVanilla("lapis_ores"), MapColor.LAPIS_BLUE.color);
        BLOCK_TAG_COLORS.put(Id.ofVanilla("redstone_ores"), MapColor.RED.color);
        BLOCK_TAG_COLORS.put(Id.ofVanilla("copper_ores"), MapColor.TERRACOTTA_ORANGE.color);
        BLOCK_TAG_COLORS.put(Id.ofVanilla("coal_ores"), MapColor.GRAY.color);
        BLOCK_TAG_COLORS.put(Id.ofVanilla("emerald_ores"), MapColor.EMERALD_GREEN.color);
        BLOCK_TAG_COLORS.put(Id.ofVanilla("iron_ores"), MapColor.BROWN.color);
        BLOCK_TAG_COLORS.put(Id.ofVanilla("diamond_ores"), MapColor.DIAMOND_BLUE.color);
        BLOCK_TAG_COLORS.put(Id.ofVanilla("gold_ores"), MapColor.GOLD.color);
    }
}
