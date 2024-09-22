package com.midnightbits.scanner.test.mocks.networking;

import com.midnightbits.scanner.rt.event.Event;
import com.midnightbits.scanner.rt.event.EventEmitter;
import com.midnightbits.scanner.rt.event.EventListener;
import com.midnightbits.scanner.rt.event.MapEventEmitter;
import com.midnightbits.scanner.rt.networking.ClientPlayMessaging;
import com.midnightbits.scanner.rt.networking.Message;
import com.midnightbits.scanner.rt.networking.event.ChannelIsAvailable;

public class MockClientPlayMessaging implements ClientPlayMessaging {
    private EventEmitter emitter = new MapEventEmitter();
    private MockPlayMessagingBridge bridge;

    public MockClientPlayMessaging(MockPlayMessagingBridge bridge) {
        this.bridge = bridge;
    }

    public MockPlayMessagingBridge bridge() {
        return bridge;
    }

    @Override
    public <T extends Event> void addEventListener(Class<T> type, EventListener<T> listener) {
        emitter.addEventListener(type, listener);
    }

    @Override
    public <T extends Event> void removeEventListener(Class<T> type, EventListener<T> listener) {
        emitter.removeEventListener(type, listener);
    }

    @Override
    public <T extends Event> void dispatchEvent(T event) {
        emitter.dispatchEvent(event);
    }

    @Override
    public <T extends Message> void registerGlobalHandler(Message.Ref<T> ref, Class<T> klass,
            ClientPlayMessaging.Receiver<T> receiver) {
        bridge.registerClientReceiver(ref, klass, receiver);
        dispatchEvent(new ChannelIsAvailable(ref.id()));
    }

    @Override
    public <T extends Message> void send(T payload) {
        bridge.sendMessageToServer(payload);
    }
}
