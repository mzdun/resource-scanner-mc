package com.midnightbits.scanner.rt.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapEventEmitter implements EventEmitter {
    private Map<Class<?>, List<EventListener<?>>> listeners = new HashMap<>();

    @Override
    public <T extends Event> void addEventListener(Class<T> type, EventListener<T> listener) {
        var clazzListeners = listeners.computeIfAbsent(type, k -> new ArrayList<>());
        clazzListeners.add(listener);
    }

    @Override
    public <T extends Event> void removeEventListener(Class<T> type, EventListener<T> listener) {
        var clazzListeners = listeners.get(type);
        if (clazzListeners == null) {
            return;
        }
        clazzListeners.remove(listener);
    }

    @Override
    public void dispatchEvent(Event event) {
        assert event != null;
        var clazz = event.getClass();
        var clazzListeners = listeners.get(clazz);
        if (clazzListeners == null) {
            return;
        }

        apply(clazzListeners, event);
    }

}
