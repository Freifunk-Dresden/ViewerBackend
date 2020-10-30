package de.freifunkdresden.viewerbackend.exception;

public class HTTPStatusCodeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int statusCode;

    public HTTPStatusCodeException(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String getMessage() {
        return String.format("status code: %d", statusCode);
    }
}
