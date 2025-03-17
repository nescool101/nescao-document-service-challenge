package com.clara.ops.challenge.document_management_service_challenge.exception;

/**
 * Exception thrown when file validation fails. This includes cases where the file is empty or not a
 * PDF.
 */
public class InvalidFileException extends RuntimeException {
  public InvalidFileException(String message) {
    super(message);
  }
}
