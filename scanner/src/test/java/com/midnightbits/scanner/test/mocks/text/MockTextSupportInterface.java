package com.midnightbits.scanner.test.mocks.text;

import com.midnightbits.scanner.rt.text.MutableText;
import com.midnightbits.scanner.rt.text.Text;
import com.midnightbits.scanner.rt.text.TextSupportInterface;

import java.util.ArrayList;
import java.util.List;

public class MockTextSupportInterface implements TextSupportInterface {
    @Override
    public MutableText literal(String string) {
        return new MockText(new LiteralSegment(string));
    }

    private interface Segment {
        String getString();
    }

    private record LiteralSegment(String contents) implements Segment {

        @Override
        public String getString() {
            return contents;
        }
    }

    private record TextSegment(Text contents) implements Segment {

        @Override
        public String getString() {
            return contents.getString();
        }
    }

    private static class MockText implements MutableText {
        private final List<Segment> segments = new ArrayList<>();

        public MockText(Segment segment) {
            append(segment);
        }

        @Override
        public String getString() {
            if (segments.isEmpty())
                return "";

            if (segments.size() == 1)
                return segments.getFirst().getString();

            StringBuilder builder = new StringBuilder();
            for (Segment segment : segments) {
                builder.append(segment.getString());
            }
            return builder.toString();
        }

        @Override
        public MutableText append(Text chunk) {
            return append(new TextSegment(chunk));

        }

        @Override
        public MutableText append(String literal) {
            return append(new LiteralSegment(literal));
        }

        public MutableText append(Segment segment) {
            segments.add(segment);
            return this;
        }

        @Override
        public MutableText formattedGold() {
            return this;
        }
    };
}
