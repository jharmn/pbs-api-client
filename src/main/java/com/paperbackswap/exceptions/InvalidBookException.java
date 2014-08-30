package com.paperbackswap.exceptions;

public class InvalidBookException extends Exception {
    private static final long serialVersionUID = 1979184620986079307L;
    public InvalidBookException() {
        super("Unable to process book");
    }
}
