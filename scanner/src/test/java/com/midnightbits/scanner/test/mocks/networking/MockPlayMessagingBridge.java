package com.midnightbits.scanner.test.mocks.networking;

import java.util.HashMap;
import java.util.Map;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.networking.ClientPlayMessaging;
import com.midnightbits.scanner.rt.networking.ServerPlayMessaging;
import com.midnightbits.scanner.rt.networking.Message;
import com.midnightbits.scanner.rt.networking.MessageSender;

public class MockPlayMessagingBridge {

    private class ClientContext implements ClientPlayMessaging.Context, MessageSender {

        @Override
        public MessageSender responseSender() {
            return this;
        }

        @Override
        public <T extends Message> void sendMessage(T message) {
            sendMessageToServer(message);
        }
    }

    private class ServerContext implements ServerPlayMessaging.Context, MessageSender {

        @Override
        public MessageSender responseSender() {
            return this;
        }

        @Override
        public <T extends Message> void sendMessage(T message) {
            sendMessageToClient(message);
        }
    }

    private interface ClientPlayReceiverReg {
        void receive(Message message, ClientPlayMessaging.Context context);
    };

    private interface ServerPlayReceiverReg {
        void receive(Message message, ServerPlayMessaging.Context context);
    };

    final Map<Id, ClientPlayReceiverReg> clientReceivers = new HashMap<>();
    final Map<Id, ServerPlayReceiverReg> serverReceivers = new HashMap<>();
    final ClientContext clientContext = this.new ClientContext();
    final ServerContext serverContext = this.new ServerContext();

    @SuppressWarnings("unchecked")
    public <T extends Message> void registerClientReceiver(Message.Ref<T> ref, Class<T> klass,
            ClientPlayMessaging.Receiver<T> receiver) {
        clientReceivers.put(ref.id(), (message, context) -> {
            receiver.receive((T) message, context);
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends Message> void registerServerReceiver(Message.Ref<T> ref, Class<T> klass,
            ServerPlayMessaging.Receiver<T> receiver) {
        serverReceivers.put(ref.id(), (message, context) -> {
            receiver.receive((T) message, context);
        });
    }

    public <T extends Message> void sendMessageToServer(T message) {
        final var receiver = serverReceivers.get(message.getRef().id());
        if (receiver == null)
            return;

        receiver.receive(message, serverContext);
    }

    public <T extends Message> void sendMessageToClient(T message) {
        final var receiver = clientReceivers.get(message.getRef().id());
        if (receiver == null)
            return;

        receiver.receive(message, clientContext);
    }

    public ClientPlayMessaging.Context clientContext() {
        return clientContext;
    }

    public ServerPlayMessaging.Context serverContext() {
        return serverContext;
    }
}
