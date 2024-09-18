package com.midnightbits.scanner.rt.event;

public class Event {
    private boolean cancelable = true;
    private boolean cancelled = false;

    public Event() {
    }

    public Event(boolean cancelable) {
        this.cancelable = cancelable;
    }

    public boolean cancelable() {
        return cancelable;
    }

    public boolean cancelled() {
        return cancelled;
    }

    public void cancel() {
        if (cancelable)
            cancelled = true;
    }
}
