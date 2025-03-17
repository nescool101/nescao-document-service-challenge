package com.clara.ops.challenge.document_management_service_challenge.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

/**
 * Global exception handler for the application.
 * Provides centralized exception handling across all controllers.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDocumentNotFoundException(DocumentNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DocumentUploadException.class)
    public ResponseEntity<ErrorResponse> handleDocumentUploadException(DocumentUploadException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileException(InvalidFileException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Remove the conflicting MaxUploadSizeExceededException handler and override the parent method instead
    // Fix the method signature to match the parent class
        @Override
        protected ResponseEntity<Object> handleExceptionInternal(
                Exception ex, 
                Object body, 
                org.springframework.http.HttpHeaders headers,
                org.springframework.http.HttpStatusCode status, 
                WebRequest request) {
            
            // Special handling for MaxUploadSizeExceededException
            if (ex instanceof MaxUploadSizeExceededException) {
                ErrorResponse errorResponse = new ErrorResponse(
                        HttpStatus.PAYLOAD_TOO_LARGE.value(),
                        "File size exceeds the maximum allowed limit",
                        LocalDateTime.now()
                );
                return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
            }
            
            // For other exceptions, create a standard error response
            ErrorResponse errorResponse = new ErrorResponse(
                    status instanceof HttpStatus ? ((HttpStatus) status).value() : status.value(),
                    ex.getMessage(),
                    LocalDateTime.now()
            );
            return new ResponseEntity<>(errorResponse, status);
        }

    // Keep the generic exception handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred: " + ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Standard error response format for all exceptions.
     */
    @Data
    @AllArgsConstructor
    static class ErrorResponse {
        private int status;
        private String message;
        private LocalDateTime timestamp;
    }
}