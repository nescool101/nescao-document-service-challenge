package com.clara.ops.challenge.document_management_service_challenge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Exception thrown when a document is not found. */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class DocumentNotFoundException extends RuntimeException {
  public DocumentNotFoundException(Long documentId) {
    super("Document not found with id: " + documentId);
  }
}
