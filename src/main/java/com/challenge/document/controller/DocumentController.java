package com.challenge.document.controller;

import com.challenge.document.dto.DocumentUploadRequest;
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


@RestController
@RequestMapping("/api/documents")
@Slf4j
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Document> uploadDocument(
            @RequestPart("metadata") DocumentUploadRequest request,
            @RequestPart("file") MultipartFile file) {
        Document document = documentService.uploadDocument(request, file);
        return ResponseEntity.ok(document);
    }
}