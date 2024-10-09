// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils.test.gl;

import java.util.List;

public class SubVerifier implements VertexVerifier {
    final List<VertexVerifier> items;
    final String thisEntry;

    SubVerifier(List<VertexVerifier> items, String thisEntry) {
        this.items = items;
        this.thisEntry = thisEntry;
    }

    public int verticesNeeded() {
        return items.stream().map(VertexVerifier::verticesNeeded).reduce(0, Integer::sum);
    }

    public String thisCallstackEntry() {
        return thisEntry;
    }

    public void assertTape(VertexTape tape, String stack) {
        for (final var item : items)
            item.assertTape(tape, stack + "\n" + thisEntry);
    }
}
