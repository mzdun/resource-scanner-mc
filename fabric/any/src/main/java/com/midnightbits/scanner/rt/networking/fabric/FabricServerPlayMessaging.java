package com.midnightbits.scanner.rt.networking.fabric;

import java.util.HashSet;
import java.util.Set;

import com.midnightbits.scanner.rt.networking.Message;
import com.midnightbits.scanner.rt.networking.MessageSender;
import com.midnightbits.scanner.rt.networking.event.ChannelIsAvailable;
import com.midnightbits.scanner.rt.networking.event.ConnectionEstablished;
import com.midnightbits.scanner.rt.core.fabric.Minecraft;
import com.midnightbits.scanner.rt.event.Event;
import com.midnightbits.scanner.rt.event.EventEmitter;
import com.midnightbits.scanner.rt.event.EventListener;
import com.midnightbits.scanner.rt.event.MapEventEmitter;
import com.midnightbits.scanner.rt.networking.ServerPlayMessaging;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;

public class FabricServerPlayMessaging implements ServerPlayMessaging {
    private static record FabricContext(ServerPlayNetworking.Context context) implements Context {
        @Override
        public MessageSender responseSender() {
            return FabricMessageSender.wrap(context.responseSender());
        }

        public static Context wrap(ServerPlayNetworking.Context context) {
            return new FabricContext(context);
        }
    };

    private interface GlobalReceiverData {
        void register();
    };

    private class GlobalReceiverDataImpl<T extends Message> implements GlobalReceiverData {
        final CustomPayload.Id<GenericPayload> id;
        final Receiver<T> receiver;

        public GlobalReceiverDataImpl(CustomPayload.Id<GenericPayload> id, Receiver<T> receiver) {
            this.id = id;
            this.receiver = receiver;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void register() {
            ServerPlayNetworking.registerGlobalReceiver(id,
                    (payload, context) -> {
                        receiver.receive((T) payload.getPayload(), FabricContext.wrap(context));
                    });
        }
    };

    private Set<GlobalReceiverData> globalPlayReceiversToRegister = new HashSet<>();
    private EventEmitter emitter = new MapEventEmitter();

    public void setup() {
        ServerPlayConnectionEvents.INIT.register((handler, client) -> {
            Set<GlobalReceiverData> copy;

            synchronized (globalPlayReceiversToRegister) {
                copy = new HashSet<>(globalPlayReceiversToRegister);
                globalPlayReceiversToRegister.clear();
            }

            for (final var receiver : copy) {
                receiver.register();
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            emitter.dispatchEvent(new ConnectionEstablished(FabricMessageSender.wrap(sender)));
        });

        S2CPlayChannelEvents.REGISTER.register((handler, sender, client, channels) -> {
            for (final var channel : channels) {
                final var id = Minecraft.idOf(channel);
                emitter.dispatchEvent(new ChannelIsAvailable(id));
            }
        });
    }

    @Override
    public <T extends Message> void registerGlobalHandler(Message.Ref<T> ref, Class<T> klass,
            Receiver<T> receiver) {

        final var builder = new GenericPayload(klass, ref);

        synchronized (globalPlayReceiversToRegister) {
            PayloadTypeRegistry.playS2C().register(builder.getId(), builder.createCodec());
            globalPlayReceiversToRegister.add(new GlobalReceiverDataImpl<>(builder.getId(), receiver));
        }
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
}
