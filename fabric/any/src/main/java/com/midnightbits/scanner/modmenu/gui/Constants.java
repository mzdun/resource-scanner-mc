// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.modmenu.gui;

import com.midnightbits.scanner.rt.core.ScannerMod;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Constants {
    String TAG = ScannerMod.MOD_ID;
    Logger LOGGER = LoggerFactory.getLogger(TAG);

    TagKey<Item> ORES_TAG = TagKey.of(Registries.ITEM.getKey(), Identifier.of("c:ores"));
    Identifier ITEMS_BG = Identifier.ofVanilla("textures/gui/container/creative_inventory/tab_items.png");

    int ROWS_COUNT = 5;
    int AT_HAND_ROWS_COUNT = 2;
    int COLUMNS_COUNT = 9;
    int DX = 9;
    int TOP = 18;
    int BOTTOM = 6;
    int BUTTON_SIZE = 18;
    int ICON_SIZE = 16;
    int VERTICAL_SPACE = 4;

    int SCROLLBAR_SEP = 3;
    int SCROLLBAR_WIDTH = 12;
    int SCROLLBAR_HEIGHT = 15;

    int TEXTURE_HEIGHT = TOP + ROWS_COUNT * BUTTON_SIZE + VERTICAL_SPACE + BUTTON_SIZE + BOTTOM;
    int INVENTORY_HEIGHT = TEXTURE_HEIGHT + BUTTON_SIZE;
    int INVENTORY_WIDTH = DX + COLUMNS_COUNT * BUTTON_SIZE + SCROLLBAR_SEP + SCROLLBAR_WIDTH + DX;

    int OPTION_SEP = 4;
    int WARNING_OPTION_SEP = 10;
    int ITEM_HEIGHT = 25;
    int ITEM_CONTENT_HEIGHT = ITEM_HEIGHT - OPTION_SEP;
    int CONTENT_WIDTH = 220;
}
