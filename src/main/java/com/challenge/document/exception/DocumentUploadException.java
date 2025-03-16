package com.challenge.document.exception;

/**
 * Exception thrown when document upload operations fail.
 * This could be due to MinIO storage issues or other upload-related problems.
 */
public class DocumentUploadException extends RuntimeException {
    public DocumentUploadException(String message) {
        super(message);
    }

    public DocumentUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}