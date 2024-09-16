package com.midnightbits.scanner.rt.core.fabric;

import com.midnightbits.scanner.rt.core.BlockInfo;
import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.text.MutableText;
import com.midnightbits.scanner.utils.CashableValue;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class MinecraftBlockInfo implements BlockInfo {
    BlockState state;
    Block block;
    CashableValue<Boolean> blockIsAir = CashableValue.of(() -> state.isAir());
    CashableValue<Identifier> id = CashableValue.of(() -> Registries.BLOCK.getId(block));
    CashableValue<MutableText> name = CashableValue.of(() -> Minecraft.packText(block.getName()));

    MinecraftBlockInfo(BlockState state, Block block) {
        this.state = state;
        this.block = block;
    }

    @Override
    public boolean isAir() {
        return blockIsAir.get();
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
