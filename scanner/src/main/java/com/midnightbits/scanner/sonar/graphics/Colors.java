package com.midnightbits.scanner.sonar.graphics;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.core.Services;

import java.util.Map;

public interface Colors {
    int CLEAR = 0x000000;
    int VANILLA = 0xF3E5AB;
    int PALE_GREEN = 0x7FB238;
    int PALE_YELLOW = 0xF7E9A3;
    int WHITE_GRAY = 0xC7C7C7;
    int BRIGHT_RED = 0xFF0000;
    int PALE_PURPLE = 0xA0A0FF;
    int IRON_GRAY = 0xA7A7A7;
    int DARK_GREEN = 0x007C00;
    int WHITE = 0xFFFFFF;
    int LIGHT_BLUE_GRAY = 0xA4A8B8;
    int DIRT_BROWN = 0x976D4D;
    int STONE_GRAY = 0x707070;
    int WATER_BLUE = 0x4040FF;
    int OAK_TAN = 0x8F7748;
    int OFF_WHITE = 0xFFFCF5;
    int ORANGE = 0xD87F33;
    int MAGENTA = 0xB24CD8;
    int LIGHT_BLUE = 0x6699D8;
    int YELLOW = 0xE5E533;
    int LIME = 0x7FCC19;
    int PINK = 0xF27FA5;
    int GRAY = 0x4C4C4C;
    int LIGHT_GRAY = 0x999999;
    int CYAN = 0x4C7F99;
    int PURPLE = 0x7F3FB2;
    int BLUE = 0x334CB2;
    int BROWN = 0x664C33;
    int GREEN = 0x667F33;
    int RED = 0x993333;
    int BLACK = 0x191919;
    int GOLD = 0xFAEE4D;
    int DIAMOND_BLUE = 0x5CDBD5;
    int LAPIS_BLUE = 0x4A80FF;
    int EMERALD_GREEN = 0x00D93A;
    int SPRUCE_BROWN = 0x815631;
    int DARK_RED = 0x700200;
    int TERRACOTTA_WHITE = 0xD1B1A1;
    int TERRACOTTA_ORANGE = 0x9F5224;
    int TERRACOTTA_MAGENTA = 0x95576C;
    int TERRACOTTA_LIGHT_BLUE = 0x706C8A;
    int TERRACOTTA_YELLOW = 0xBA8524;
    int TERRACOTTA_LIME = 0x677535;
    int TERRACOTTA_PINK = 0xA04D4E;
    int TERRACOTTA_GRAY = 0x392923;
    int TERRACOTTA_LIGHT_GRAY = 0x876B62;
    int TERRACOTTA_CYAN = 0x575C5C;
    int TERRACOTTA_PURPLE = 0x7A4958;
    int TERRACOTTA_BLUE = 0x4C3E5C;
    int TERRACOTTA_BROWN = 0x4C3223;
    int TERRACOTTA_GREEN = 0x4C522A;
    int TERRACOTTA_RED = 0x8E3C2E;
    int TERRACOTTA_BLACK = 0x251610;
    int DULL_RED = 0xBD3031;
    int DULL_PINK = 0x943F61;
    int DARK_CRIMSON = 0x5C191D;
    int TEAL = 0x167E86;
    int DARK_AQUA = 0x3A8E8C;
    int DARK_DULL_PINK = 0x562C3E;
    int BRIGHT_TEAL = 0x14B485;
    int DEEPSLATE_GRAY = 0x646464;
    int RAW_IRON_PINK = 0xD8AF93;
    int LICHEN_GREEN = 0x7FA796;

    int ECHO_ALPHA = 0x80000000;
    int RGB_MASK = WHITE;

    Map<Id, Integer> BLOCK_TAG_COLORS = Services.PLATFORM.getBlockTagColors();
}
