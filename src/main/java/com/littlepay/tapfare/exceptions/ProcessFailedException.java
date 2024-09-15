package com.littlepay.tapfare.exceptions;

public class ProcessFailedException extends RuntimeException {
    public ProcessFailedException(final String errorMessage, final Throwable err) {
        super(errorMessage, err);
    }
}
