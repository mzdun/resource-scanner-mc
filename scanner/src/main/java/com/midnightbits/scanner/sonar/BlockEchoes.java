package com.midnightbits.scanner.sonar;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.math.V3i;

public class BlockEchoes implements Iterable<BlockEcho> {
    private TreeSet<BlockEcho> echoes = new TreeSet<BlockEcho>();
    private static final int MAX_SIZE = 100;

    private final int maxSize;

    public BlockEchoes(int maxSize) {
        this.maxSize = maxSize;
    }

    public BlockEchoes() {
        this(MAX_SIZE);
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
        boolean replaced = evictBlocks(stream().filter(b -> b.getPosition().equals(position)));
        if (echoes.size() >= maxSize) {
            evictBlocks(stream().limit(echoes.size() - maxSize + 1));
        }

        BlockEcho echo = BlockEcho.echoFrom(position, id);
        echoes.add(echo);
        return !replaced;
    }

    private Stream<BlockEcho> stream() {
        return StreamSupport.stream(echoes.spliterator(), false);
    }

    private boolean evictBlocks(Stream<BlockEcho> stream) {
        List<BlockEcho> evictions = stream
                .collect(Collectors.toList());
        for (BlockEcho evicted : evictions) {
            echoes.remove(evicted);
        }

        return evictions.size() > 0;
    }

    @Override
    public Iterator<BlockEcho> iterator() {
        return echoes.iterator();
    }

}
