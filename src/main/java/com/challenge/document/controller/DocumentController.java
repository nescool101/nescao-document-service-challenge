package com.challenge.document.controller;

import com.challenge.document.dto.DocumentUploadRequest;
import com.challenge.document.exception.DocumentUploadException;
import com.challenge.document.exception.InvalidFileException;
import com.challenge.document.model.Document;
import com.challenge.document.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


/**
 * REST Controller for handling document-related operations.
 * Provides endpoints for uploading, retrieving, and managing documents.
 */
@RestController
@RequestMapping("/api/documents")
@Slf4j
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Handles document upload requests.
     *
     * @param request The metadata for the document being uploaded
     * @param file The PDF file to be uploaded
     * @return ResponseEntity containing the saved document metadata
     * @throws DocumentUploadException if the upload fails
     * @throws InvalidFileException if the file is invalid
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Document> uploadDocument (
            @RequestPart("metadata") DocumentUploadRequest request,
            @RequestPart("file") MultipartFile file) {
        Document document = documentService.uploadDocument(request, file);
        return ResponseEntity.ok(document);
    }
}