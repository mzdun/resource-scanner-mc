package com.midnightbits.scanner.rt.networking;

public interface MessageSender {
    <T extends Message> void sendMessage(T message);
}
