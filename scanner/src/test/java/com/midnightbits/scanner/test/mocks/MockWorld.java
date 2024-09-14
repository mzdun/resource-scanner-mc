package com.midnightbits.scanner.test.mocks;

import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

import com.midnightbits.scanner.rt.core.BlockInfo;
import com.midnightbits.scanner.rt.math.V3i;

public class MockWorld {
    public TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, BlockInfo>>> layers = new TreeMap<>();
    private static final BlockInfo AIR = MockBlockInfo.ofAir();

    private static record Block(V3i pos, BlockInfo info) {
        public static Block of(String encoded) {
            try {
                char colon = ':';
                int prev = 0;
                int pos = encoded.indexOf(colon, prev);
                if (pos <= prev)
                    return null;
                int x = Integer.parseInt(encoded.substring(prev, pos));
                prev = pos + 1;

                pos = encoded.indexOf(colon, prev);
                if (pos <= prev)
                    return null;
                int y = Integer.parseInt(encoded.substring(prev, pos));
                prev = pos + 1;

                pos = encoded.indexOf(colon, prev);
                if (pos <= prev)
                    return null;
                int z = Integer.parseInt(encoded.substring(prev, pos));
                prev = pos + 1;

                pos = encoded.indexOf(colon, prev);
                if (pos == prev)
                    return null;

                if (pos < 0) {
                    int caveFlag = Integer.parseInt(encoded.substring(prev, pos));
                    if (caveFlag == 0)
                        return null;
                    return new Block(new V3i(x, y, z), MockBlockInfo.ofCaveAir());
                }

                int caveFlag = Integer.parseInt(encoded.substring(prev, pos));
                prev = pos + 1;

                BlockInfo info = MockBlockInfo.of(encoded.substring(prev));
                if (caveFlag == 1 || info == null)
                    return null;
                return new Block(new V3i(x, y, z), info);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    public static MockWorld of(String text) {
        MockWorld result = new MockWorld();
        text.lines().forEach(line -> {
            Block info = Block.of(line);
            if (info != null)
                result.add(info);
        });
        return result;
    }

    public static MockWorld ofResource(String name) {
        try {
            InputStream is = MockWorld.class.getClassLoader().getResourceAsStream(name);
            byte[] bytes = is.readAllBytes();
            String contents = new String(bytes, "UTF-8");
            return of(contents);
        } catch (IOException e) {
            return new MockWorld();
        }
    }

    public void add(Block info) {
        int i = info.pos.getX();
        int j = info.pos.getY();
        int k = info.pos.getZ();

        add(i, j, k, info.info);
    }

    public void add(int i, int j, int k, BlockInfo info) {
        layers.computeIfAbsent(j, key -> new TreeMap<>());
        TreeMap<Integer, TreeMap<Integer, BlockInfo>> layer = layers.get(j);

        layer.computeIfAbsent(i, key -> new TreeMap<>());
        TreeMap<Integer, BlockInfo> line = layer.get(i);

        line.put(k, info);
    }

    public BlockInfo get(V3i pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();

        TreeMap<Integer, TreeMap<Integer, BlockInfo>> layer = layers.getOrDefault(j, null);
        if (layer == null)
            return null;

        TreeMap<Integer, BlockInfo> line = layer.getOrDefault(i, null);
        if (line == null)
            return null;

        return line.getOrDefault(k, null);
    }

    public BlockInfo getOrAir(V3i pos) {
        BlockInfo block = get(pos);
        return block == null ? AIR : block;
    }
}
