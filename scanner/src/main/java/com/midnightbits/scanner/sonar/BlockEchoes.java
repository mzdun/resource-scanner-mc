// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.utils.Clock;

import org.jetbrains.annotations.NotNull;

public final class BlockEchoes implements Iterable<BlockEcho> {
    private final TreeSet<BlockEcho> echoes = new TreeSet<>();
    public static final int MAX_SIZE = 100;
    public static final int ECHO_LIFETIME = 10000;

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
     * @return resulting block
     */
    public BlockEcho echoFrom(V3i position, Id id) {
        return echoFrom(new BlockEcho.Partial(position, id));
    }

    /**
     * Adds pinged block to list of all seen blocks.
     *
     * @param partial where the ping was registered and what did the echo bounced
     *                off of
     * @return resulting block
     */
    public BlockEcho echoFrom(BlockEcho.Partial partial) {
        evictBlocks(stream().filter(b -> b.position().equals(partial.position())));
        if (echoes.size() >= maxSize) {
            evictBlocks(stream().limit(echoes.size() - maxSize + 1));
        }

        BlockEcho echo = BlockEcho.echoFrom(partial);
        echoes.add(echo);
        return echo;
    }

    public void removeOldEchoes() {
        final var now = Clock.currentTimeMillis();
        evictBlocks(stream().filter(b -> (now - b.pingTime()) > ECHO_LIFETIME));
    }

    private Stream<BlockEcho> stream() {
        return echoes.stream();
    }

    private void evictBlocks(Stream<BlockEcho> stream) {
        List<BlockEcho> evictions = stream.toList();
        for (BlockEcho evicted : evictions) {
            echoes.remove(evicted);
        }
    }

    @NotNull
    @Override
    public Iterator<BlockEcho> iterator() {
        return echoes.iterator();
    }
}
