package com.midnightbits.scanner.rt.networking;

import com.midnightbits.scanner.rt.event.EventEmitter;

public interface ServerPlayMessaging extends EventEmitter {
    interface Context {
        MessageSender responseSender();
    }

    @FunctionalInterface
    public interface Receiver<T extends Message> {
        void receive(T payload, Context context);
    }

    public <T extends Message> void registerGlobalHandler(Message.Ref<T> ref, Class<T> klass,
            Receiver<T> receiver);
}
