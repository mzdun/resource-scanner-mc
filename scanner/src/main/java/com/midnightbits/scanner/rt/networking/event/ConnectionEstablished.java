package com.midnightbits.scanner.rt.networking.event;

import com.midnightbits.scanner.rt.event.Event;
import com.midnightbits.scanner.rt.networking.MessageSender;

public class ConnectionEstablished extends Event {
    final MessageSender sender;

    public ConnectionEstablished(MessageSender sender) {
        super();
        this.sender = sender;
    }

    public MessageSender sender() {
        return sender;
    }
}
