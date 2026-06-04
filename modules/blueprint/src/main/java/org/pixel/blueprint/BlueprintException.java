package org.pixel.blueprint;

public class BlueprintException extends RuntimeException {

    public BlueprintException(String message) {
        super(message);
    }

    public BlueprintException(String message, Throwable cause) {
        super(message, cause);
    }

    public BlueprintException(Throwable cause) {
        super(cause);
    }
}
