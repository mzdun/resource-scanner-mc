package com.midnightbits.scanner.rt.event;

import java.lang.reflect.ParameterizedType;

public interface EventEmitterOf<T extends Event> {
    void addEventListener(EventListener<T> listener);

    void removeEventListener(EventListener<T> listener);

    public class Impl<U extends Event> implements EventEmitterOf<U> {
        EventEmitter events = EventEmitter.createEmitter();

        @SuppressWarnings("unchecked")
        private Class<U> clazz() {
            final var superclass = (ParameterizedType) getClass().getGenericSuperclass();

            return (Class<U>) superclass.getActualTypeArguments()[0];
        }

        @Override
        public void addEventListener(EventListener<U> listener) {
            events.addEventListener(clazz(), listener);
        }

        @Override
        public void removeEventListener(EventListener<U> listener) {
            events.removeEventListener(clazz(), listener);
        }

        protected void dispatchEvent(U event) {
            events.dispatchEvent(event);
        }

    }

}
