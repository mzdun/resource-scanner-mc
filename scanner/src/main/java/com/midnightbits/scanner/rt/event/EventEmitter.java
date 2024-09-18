package com.midnightbits.scanner.rt.event;

public interface EventEmitter {
    <T extends Event> void addEventListener(Class<T> type, EventListener<T> listener);

    <T extends Event> void removeEventListener(Class<T> type, EventListener<T> listener);

    <T extends Event> void dispatchEvent(T event);

    static EventEmitter createEmitter() {
        return new MapEventEmitter();
    }

    @SuppressWarnings("unchecked")
    default void apply(Iterable<EventListener<?>> listeners, Event event) {
        for (var listener : listeners) {
            if (event.cancelable() && event.cancelled()) {
                break;
            }
            ((EventListener<Event>) listener).apply(event);
        }
    }
}
