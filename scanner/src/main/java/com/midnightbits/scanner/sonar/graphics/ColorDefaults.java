// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.graphics;

import com.midnightbits.scanner.rt.core.Id;

import java.util.HashMap;
import java.util.Map;

public class ColorDefaults {
    public static final Map<Id, Integer> BLOCK_TAG_COLORS;

    private ColorDefaults() {}

    static {
        BLOCK_TAG_COLORS = new HashMap<>();
        BLOCK_TAG_COLORS.put(Id.ofVanilla("lapis_ores"), Colors.LAPIS_BLUE);
        BLOCK_TAG_COLORS.put(Id.ofVanilla("redstone_ores"), Colors.RED);
        BLOCK_TAG_COLORS.put(Id.ofVanilla("copper_ores"), Colors.TERRACOTTA_ORANGE);
        BLOCK_TAG_COLORS.put(Id.ofVanilla("coal_ores"), Colors.GRAY);
        BLOCK_TAG_COLORS.put(Id.ofVanilla("emerald_ores"), Colors.EMERALD_GREEN);
        BLOCK_TAG_COLORS.put(Id.ofVanilla("iron_ores"), Colors.BROWN);
        BLOCK_TAG_COLORS.put(Id.ofVanilla("diamond_ores"), Colors.DIAMOND_BLUE);
        BLOCK_TAG_COLORS.put(Id.ofVanilla("gold_ores"), Colors.GOLD);
    }
}
