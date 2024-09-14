package com.midnightbits.scanner.rt.core;

import org.apache.commons.text.StringEscapeUtils;

public class InvalidIdentifierException extends RuntimeException {
    public InvalidIdentifierException(String message) {
        super(StringEscapeUtils.escapeJava(message));
    }

    public InvalidIdentifierException(String message, Throwable throwable) {
        super(StringEscapeUtils.escapeJava(message), throwable);
    }
}
