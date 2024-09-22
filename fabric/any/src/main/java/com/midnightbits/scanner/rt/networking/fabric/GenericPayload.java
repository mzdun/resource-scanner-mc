package com.midnightbits.scanner.rt.networking.fabric;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import com.midnightbits.scanner.rt.core.fabric.Minecraft;
import com.midnightbits.scanner.rt.networking.Message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public class GenericPayload implements CustomPayload {
    final Message payload;
    final Class<?> klass;
    final CustomPayload.Id<GenericPayload> id;

    public GenericPayload(Message payload, Class<?> klass, Message.Ref<?> ref) {
        this.payload = payload;
        this.klass = klass;
        this.id = new CustomPayload.Id<>(Minecraft.identifierOf(ref.id()));
    }

    public GenericPayload(Message payload) {
        this(payload, payload.getClass(), payload.getRef());
    }

    public GenericPayload(Class<?> klass, Message.Ref<?> ref) {
        this(null, klass, ref);
    }

    public GenericPayload(Class<?> klass) throws NoSuchFieldException {
        this(null, klass, refFromClass(klass));
    }

    static GenericPayload fromPacket(Object decoded) {
        if (decoded instanceof Message message) {
            return new GenericPayload(message);
        }
        return null;
    }

    public static <T extends Message> GenericPayload of(T payload) {
        return new GenericPayload(payload);
    }

    public Message getPayload() {
        return payload;
    }

    @Override
    public CustomPayload.Id<GenericPayload> getId() {
        return id;
    }

    private static Message.Ref<?> refFromClass(Class<?> klass) throws NoSuchFieldException {
        final var refField = klass.getDeclaredField("REF");
        try {
            final var refObj = refField.get(null);
            return (Message.Ref<?>) refObj;
        } catch (ReflectiveOperationException e) {
            throw new NoSuchFieldException();
        }
    }

    public PacketCodec<ByteBuf, GenericPayload> createCodec() {
        return PacketCodec.tuple(createInnerCodec(), GenericPayload::getPayload, GenericPayload::fromPacket);
    }

    private PacketCodec<ByteBuf, Object> createInnerCodec() {
        final var getters = Stream.of(klass.getDeclaredFields()).map((field) -> {
            if (Modifier.isStatic(field.getModifiers()))
                return null;

            final var type = field.getType();
            final var name = field.getName();

            try {
                final var straightGetter = klass.getDeclaredMethod(name);
                if (straightGetter.getReturnType().equals(type))
                    return straightGetter;
            } catch (NoSuchMethodException ignored) {
            }

            try {
                String renamed = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
                final var renamedGetter = klass.getDeclaredMethod(renamed);
                if (renamedGetter.getReturnType().equals(type))
                    return renamedGetter;
            } catch (NoSuchMethodException ignored) {
            }

            return null;
        }).filter(Objects::nonNull).map(PacketGetter::of).toList();

        final var ctors = klass.getDeclaredConstructors();

        Constructor<?> target = null;
        for (final var ctor : ctors) {
            final var params = ctor.getParameterTypes();

            var matchingConstructor = params.length == getters.size();
            if (matchingConstructor) {
                for (var index = 0; index < params.length; ++index) {
                    final var param = params[index];
                    final var result = getters.get(index).getReturnType();
                    if (!param.equals(result)) {
                        matchingConstructor = false;
                        break;
                    }
                }
            }

            if (matchingConstructor) {
                target = ctor;
                break;
            }
        }

        if (target == null) {
            throw new CouldNotBuildPayloadFromMembers(klass);
        }

        return pack(getters, target);
    }

    private static Object construct(Constructor<?> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static PacketCodec<ByteBuf, Object> pack(List<PacketGetter<Object>> m,
            Constructor<?> constructor) {
        switch (m.size()) {
            case 1:
                return PacketCodec.tuple(
                        m.get(0).codec(), m.get(0).function(),
                        (arg0) -> construct(constructor, arg0));
            case 2:
                return PacketCodec.tuple(
                        m.get(0).codec(), m.get(0).function(),
                        m.get(1).codec(), m.get(1).function(),
                        (arg0, arg1) -> construct(constructor, arg0, arg1));
            case 3:
                return PacketCodec.tuple(
                        m.get(0).codec(), m.get(0).function(),
                        m.get(1).codec(), m.get(1).function(),
                        m.get(2).codec(), m.get(2).function(),
                        (arg0, arg1, arg2) -> construct(constructor, arg0, arg1, arg2));
            case 4:
                return PacketCodec.tuple(
                        m.get(0).codec(), m.get(0).function(),
                        m.get(1).codec(), m.get(1).function(),
                        m.get(2).codec(), m.get(2).function(),
                        m.get(3).codec(), m.get(3).function(),
                        (arg0, arg1, arg2, arg3) -> construct(constructor, arg0, arg1, arg2, arg3));
            case 5:
                return PacketCodec.tuple(
                        m.get(0).codec(), m.get(0).function(),
                        m.get(1).codec(), m.get(1).function(),
                        m.get(2).codec(), m.get(2).function(),
                        m.get(3).codec(), m.get(3).function(),
                        m.get(4).codec(), m.get(4).function(),
                        (arg0, arg1, arg2, arg3, arg4) -> construct(constructor, arg0, arg1, arg2, arg3, arg4));
            case 6:
                return PacketCodec.tuple(
                        m.get(0).codec(), m.get(0).function(),
                        m.get(1).codec(), m.get(1).function(),
                        m.get(2).codec(), m.get(2).function(),
                        m.get(3).codec(), m.get(3).function(),
                        m.get(4).codec(), m.get(4).function(),
                        m.get(5).codec(), m.get(5).function(),
                        (arg0, arg1, arg2, arg3, arg4, arg5) -> construct(constructor, arg0, arg1, arg2, arg3, arg4,
                                arg5));
        }

        throw new TupleSizeUnsupportedException(m.size());
    }

    private static class PacketGetter<T> {
        final PacketCodec<ByteBuf, Object> codec;
        final Method accessor;

        public PacketGetter(PacketCodec<ByteBuf, Object> codec, Method accessor) {
            this.codec = codec;
            this.accessor = accessor;
        }

        @SuppressWarnings("unchecked")
        public static <T> PacketGetter<T> of(Method accessor) {
            final var codec = codecOf(accessor.getReturnType());
            return new PacketGetter<>((PacketCodec<ByteBuf, Object>) codec, accessor);
        }

        public static <T> PacketCodec<ByteBuf, ?> codecOf(Class<T> klass) {
            if (klass.equals(String.class)) {
                return PacketCodecs.STRING;
            }

            if (klass.equals(Integer.class)) {
                return PacketCodecs.INTEGER;
            }

            throw new NoCodecForClassException(klass);
        }

        public PacketCodec<ByteBuf, Object> codec() {
            return codec;
        }

        public Class<?> getReturnType() {
            return accessor.getReturnType();
        }

        public Function<Object, Object> function() {
            return (self) -> {
                try {
                    return accessor.invoke(self);
                } catch (ReflectiveOperationException e) {
                    return null;
                }
            };
        }
    }

    public static class NoCodecForClassException extends RuntimeException {
        public NoCodecForClassException(Class<?> klass) {
            super("Could not find codec for filed type " + klass.getName());
        }
    }

    public static class TupleSizeUnsupportedException extends RuntimeException {
        public TupleSizeUnsupportedException(int size) {
            super("Could not create a codec for member tuple of size " + size);
        }
    }

    public static class CouldNotBuildPayloadFromMembers extends RuntimeException {
        public CouldNotBuildPayloadFromMembers(Class<?> klass) {
            super("Could not find a constructor taking all the members inside " + klass.getName());
        }
    }
};
