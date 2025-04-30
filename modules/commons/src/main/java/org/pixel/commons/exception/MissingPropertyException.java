package org.pixel.commons.exception;

public class MissingPropertyException extends RuntimeException {
    public MissingPropertyException(String message) {
        super(message);
    }
}