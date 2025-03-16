package com.challenge.document.exception;

/**
 * Exception thrown when file validation fails.
 * This includes cases where the file is empty or not a PDF.
 */
public class InvalidFileException extends RuntimeException {
    public InvalidFileException(String message) {
        super(message);
    }

    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}