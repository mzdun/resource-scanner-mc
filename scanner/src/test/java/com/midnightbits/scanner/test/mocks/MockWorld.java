package com.midnightbits.scanner.test.mocks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

import com.midnightbits.scanner.rt.core.BlockInfo;
import com.midnightbits.scanner.rt.math.V3i;

public final class MockWorld {
    public TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, BlockInfo>>> layers = new TreeMap<>();
    private static final BlockInfo AIR = MockBlockInfo.ofAir();

    private record Block(V3i pos, BlockInfo info) {
        public static Block of(String encoded) {
            try {
                final var colon = ':';
                var prev = 0;
                var pos = encoded.indexOf(colon, prev);
                if (pos <= prev)
                    return null;
                final var x = Integer.parseInt(encoded.substring(prev, pos));
                prev = pos + 1;

                pos = encoded.indexOf(colon, prev);
                if (pos <= prev)
                    return null;
                final var y = Integer.parseInt(encoded.substring(prev, pos));
                prev = pos + 1;

                pos = encoded.indexOf(colon, prev);
                if (pos <= prev)
                    return null;
                final var z = Integer.parseInt(encoded.substring(prev, pos));
                prev = pos + 1;

                pos = encoded.indexOf(colon, prev);
                if (pos == prev)
                    return null;

                if (pos < 0) {
                    final var caveFlag = Integer.parseInt(encoded.substring(prev, pos));
                    if (caveFlag == 0)
                        return null;
                    return new Block(new V3i(x, y, z), MockBlockInfo.ofCaveAir());
                }

                final var caveFlag = Integer.parseInt(encoded.substring(prev, pos));
                prev = pos + 1;

                final var info = MockBlockInfo.of(encoded.substring(prev));
                if (caveFlag == 1)
                    return null;
                return new Block(new V3i(x, y, z), info);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    public static MockWorld of(String text) {
        final var result = new MockWorld();
        text.lines().forEach(line -> {
            final var info = Block.of(line);
            if (info != null)
                result.add(info);
        });
        return result;
    }

    public static MockWorld ofResource(String name) {
        try {
            final var is = MockWorld.class.getClassLoader().getResourceAsStream(name);
            assert is != null;
            byte[] bytes = is.readAllBytes();
            final var contents = new String(bytes, StandardCharsets.UTF_8);
            return of(contents);
        } catch (IOException e) {
            return new MockWorld();
        }
    }

    private void add(Block info) {
        final var i = info.pos.getX();
        final var j = info.pos.getY();
        final var k = info.pos.getZ();

        add(i, j, k, info.info);
    }

    public void add(int i, int j, int k, BlockInfo info) {
        layers.computeIfAbsent(j, key -> new TreeMap<>());
        final var layer = layers.get(j);

        layer.computeIfAbsent(i, key -> new TreeMap<>());
        final var line = layer.get(i);

        line.put(k, info);
    }

    public BlockInfo get(V3i pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();

        final var layer = layers.getOrDefault(j, null);
        if (layer == null)
            return null;

        final var line = layer.getOrDefault(i, null);
        if (line == null)
            return null;

        return line.getOrDefault(k, null);
    }

    public BlockInfo getOrAir(V3i pos) {
        final var block = get(pos);
        return block == null ? AIR : block;
    }
}
