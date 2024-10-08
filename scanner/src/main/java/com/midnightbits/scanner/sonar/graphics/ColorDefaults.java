// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.graphics;

import com.midnightbits.scanner.rt.core.Id;

import java.util.HashMap;
import java.util.Map;

public class ColorDefaults {
    public static final Map<Id, Colors.Proxy> BLOCK_TAG_COLORS;

    private ColorDefaults() {}

    static {
        BLOCK_TAG_COLORS = new HashMap<>();
        BLOCK_TAG_COLORS.put(Id.ofVanilla("lapis_ores"), new Colors.DirectValue(Colors.LAPIS_BLUE));
        BLOCK_TAG_COLORS.put(Id.ofVanilla("redstone_ores"), new Colors.DirectValue(Colors.RED));
        BLOCK_TAG_COLORS.put(Id.ofVanilla("copper_ores"), new Colors.DirectValue(Colors.TERRACOTTA_ORANGE));
        BLOCK_TAG_COLORS.put(Id.ofVanilla("coal_ores"), new Colors.DirectValue(Colors.GRAY));
        BLOCK_TAG_COLORS.put(Id.ofVanilla("emerald_ores"), new Colors.DirectValue(Colors.EMERALD_GREEN));
        BLOCK_TAG_COLORS.put(Id.ofVanilla("iron_ores"), new Colors.DirectValue(Colors.BROWN));
        BLOCK_TAG_COLORS.put(Id.ofVanilla("diamond_ores"), new Colors.DirectValue(Colors.DIAMOND_BLUE));
        BLOCK_TAG_COLORS.put(Id.ofVanilla("gold_ores"), new Colors.DirectValue(Colors.GOLD));
    }
}
