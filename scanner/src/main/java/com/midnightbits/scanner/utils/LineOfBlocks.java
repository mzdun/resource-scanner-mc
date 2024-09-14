package com.midnightbits.scanner.utils;

import java.util.Optional;

import com.midnightbits.scanner.rt.math.V3d;
import com.midnightbits.scanner.rt.math.V3i;

public class LineOfBlocks {
    public final V3i from;
    public final V3i to;
    public final V3i diff;

    public LineOfBlocks(V3i from, V3i to) {
        this.from = from;
        this.to = to;
        this.diff = from.subtract(to);
    }

    public static LineOfBlocks fromCamera(V3i operator, V3d camera, int distance) {
        V3i tgt = V3i.ofRounded(camera.multiply(distance));
        return new LineOfBlocks(operator, operator.add(tgt));
    }

    public static LineOfBlocks fromCamera(V3i operator, float cameraPitch, float cameraYaw, int distance) {
        return fromCamera(operator, V3d.fromPolar(cameraPitch, cameraYaw), distance);
    }

    private Callback<V3i> selectSlider() {
        int diffX = Math.abs(to.getX() - from.getX());
        int diffY = Math.abs(to.getY() - from.getY());
        int diffZ = Math.abs(to.getZ() - from.getZ());

        PosExtractor posX = pos -> pos.getX();
        PosExtractor posY = pos -> pos.getY();
        PosExtractor posZ = pos -> pos.getZ();

        if (diffX > diffY) {
            if (diffX > diffZ)
                return new Slider(from, to, posX, posY, posZ, (x, y, z) -> new V3i(x, y, z));

            return new Slider(from, to, posZ, posX, posY, (z, x, y) -> new V3i(x, y, z));
        }

        if (diffY > diffZ)
            return new Slider(from, to, posY, posX, posZ, (y, x, z) -> new V3i(x, y, z));

        return new Slider(from, to, posZ, posX, posY, (z, x, y) -> new V3i(x, y, z));
    }

    public Iterable<V3i> iterate() {
        return CallbackIterable.of(selectSlider());
    }

    private static class Domain {
        private final int startValue;
        private final int stopValue;
        private final int delta;
        private int currentValue;

        public Domain(int startValue, int stopValue) {
            int diff = stopValue - startValue;
            int delta = diff < 0 ? -1 : 1;
            this.startValue = startValue;
            this.stopValue = stopValue + delta;
            this.delta = delta;
            reset();
        }

        public void reset() {
            this.currentValue = this.startValue;
        }

        public boolean isDone() {
            return currentValue == stopValue;
        }

        public int next() {
            int tmp = currentValue;
            currentValue += delta;
            return tmp;
        }
    };

    private static class CounterDomain {
        private final double slope;
        private double dom0;
        private double counterDom0;

        public CounterDomain(int dom0, int counterDom0, int dom1, int counterDom1) {
            int domSlope = dom1 - dom0;
            int counterDomSlope = counterDom1 - counterDom0;
            slope = domSlope == 0 ? 1 : (double) counterDomSlope / (double) domSlope;
            this.dom0 = dom0;
            this.counterDom0 = counterDom0;
        }

        public double at(int dom) {
            return (dom - dom0) * slope + counterDom0;
        }
    };

    @FunctionalInterface
    private static interface PosMaker {
        public V3i build(int dom, int counter1, int counter2);
    };

    @FunctionalInterface
    private static interface PosExtractor {
        public int at(V3i pos);
    };

    private static class Slider implements Callback<V3i> {
        private final PosMaker maker;
        private final Domain dom;
        private final CounterDomain counter1;
        private final CounterDomain counter2;

        public Slider(V3i from, V3i to, PosExtractor domExtractor, PosExtractor counter1Extractor,
                PosExtractor counter2Extractor, PosMaker maker) {
            int start = domExtractor.at(from);
            int stop = domExtractor.at(to);
            this.maker = maker;
            this.dom = new Domain(start, stop);
            this.counter1 = new CounterDomain(start, counter1Extractor.at(from), stop, counter1Extractor.at(to));
            this.counter2 = new CounterDomain(start, counter2Extractor.at(from), stop, counter2Extractor.at(to));
        }

        public Optional<V3i> call() {
            if (dom.isDone())
                return Optional.empty();
            int posDom = dom.next();
            int posCounter1 = (int) Math.round(counter1.at(posDom));
            int posCounter2 = (int) Math.round(counter2.at(posDom));

            return Optional.of(maker.build(posDom, posCounter1, posCounter2));
        }
    };
}
