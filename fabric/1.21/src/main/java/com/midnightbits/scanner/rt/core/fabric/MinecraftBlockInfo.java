// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.rt.core.fabric;

import com.midnightbits.scanner.rt.core.BlockInfo;
import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.text.MutableText;
import com.midnightbits.scanner.utils.CacheableValue;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class MinecraftBlockInfo implements BlockInfo {
    BlockState state;
    Block block;
    CacheableValue<Boolean> blockIsAir = CacheableValue.of(() -> state.isAir());
    CacheableValue<Identifier> id = CacheableValue.of(() -> Registries.BLOCK.getId(block));
    CacheableValue<MutableText> name = CacheableValue.of(() -> Minecraft.packText(block.getName()));

    MinecraftBlockInfo(BlockState state, Block block) {
        this.state = state;
        this.block = block;
    }

    @Override
    public boolean isAir() {
        return blockIsAir.get();
    }

    @Override
    public boolean inTag(Id id) {
        final var entry = Registries.ITEM.getEntry(this.id.get());
        if (entry.isEmpty()) {
            return false;
        }

        final var key = TagKey.of(Registries.ITEM.getKey(), Minecraft.identifierOf(id));
        return entry.get().isIn(key);
    }

    @Override
    public Id getId() {
        return Minecraft.idOf(id.get());
    }

    @Override
    public MutableText getName() {
        return name.get();
    }

}
