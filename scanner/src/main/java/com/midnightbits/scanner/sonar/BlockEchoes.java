package com.midnightbits.scanner.sonar;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.math.V3i;
import org.jetbrains.annotations.NotNull;

public final class BlockEchoes implements Iterable<BlockEcho> {
    private final TreeSet<BlockEcho> echoes = new TreeSet<>();
    public static final int MAX_SIZE = 100;

    private int maxSize;

    public BlockEchoes(int maxSize) {
        this.maxSize = maxSize;
    }

    public BlockEchoes() {
        this(MAX_SIZE);
    }

    public void refresh(int maxSize) {
        this.maxSize = maxSize;
        if (echoes.size() >= maxSize) {
            evictBlocks(stream().limit(echoes.size() - maxSize));
        }
    }

    /**
     * Adds pinged block to list of all seen blocks.
     *
     * @param position where the ping was registered
     * @param id       what did the echo bounced off of
     * @return true, if there were previously no blocks at this location, false
     *         otherwise
     */
    public boolean echoFrom(V3i position, Id id) {
        boolean replaced = evictBlocks(stream().filter(b -> b.position().equals(position)));
        if (echoes.size() >= maxSize) {
            evictBlocks(stream().limit(echoes.size() - maxSize + 1));
        }

        BlockEcho echo = BlockEcho.echoFrom(position, id);
        echoes.add(echo);
        return !replaced;
    }

    private Stream<BlockEcho> stream() {
        return echoes.stream();
    }

    private boolean evictBlocks(Stream<BlockEcho> stream) {
        List<BlockEcho> evictions = stream.toList();
        for (BlockEcho evicted : evictions) {
            echoes.remove(evicted);
        }

        return !evictions.isEmpty();
    }

    @NotNull
    @Override
    public Iterator<BlockEcho> iterator() {
        return echoes.iterator();
    }

}
