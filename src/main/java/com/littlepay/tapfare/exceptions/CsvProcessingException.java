package com.littlepay.tapfare.exceptions;

public class CsvProcessingException extends RuntimeException {
    public CsvProcessingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
