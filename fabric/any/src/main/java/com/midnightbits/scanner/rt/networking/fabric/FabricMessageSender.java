package com.midnightbits.scanner.rt.networking.fabric;

import com.midnightbits.scanner.rt.networking.Message;
import com.midnightbits.scanner.rt.networking.MessageSender;

import net.fabricmc.fabric.api.networking.v1.PacketSender;

public record FabricMessageSender(PacketSender sender) implements MessageSender {
    @Override
    public <T extends Message> void sendMessage(T message) {
        sender.sendPacket(GenericPayload.of(message));
    }

    public static MessageSender wrap(PacketSender sender) {
        return new FabricMessageSender(sender);
    }
}
