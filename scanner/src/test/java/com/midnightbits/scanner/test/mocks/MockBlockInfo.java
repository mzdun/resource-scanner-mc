package com.midnightbits.scanner.test.mocks;

import com.midnightbits.scanner.rt.core.BlockInfo;
import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.core.Services;
import com.midnightbits.scanner.rt.text.MutableText;

public final class MockBlockInfo implements BlockInfo {
    private final boolean isAir;
    private final Id id;
    private final MutableText name;

    public MockBlockInfo(boolean isAir, Id id) {
        this.isAir = isAir;
        this.id = id;
        this.name = Services.TEXT.literal(id.getPath());
    }

    public static MockBlockInfo ofAir() {
        return new MockBlockInfo(true, Id.ofVanilla("air"));
    }

    public static MockBlockInfo ofCaveAir() {
        return new MockBlockInfo(true, Id.ofVanilla("cave_air"));
    }

    public static MockBlockInfo ofVoidAir() {
        return new MockBlockInfo(true, Id.ofVanilla("void_air"));
    }

    public static MockBlockInfo of(String unparsedId) {
        return new MockBlockInfo(false, Id.of(unparsedId));
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    @Override
    public boolean isAir() {
        return isAir;
    }

    @Override
    public Id getId() {
        return id;
    }

    @Override
    public MutableText getName() {
        return name;
    }
}
