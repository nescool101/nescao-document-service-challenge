package com.challenge.document.controller;

import com.challenge.document.dto.DocumentUploadRequest;
import com.challenge.document.dto.DownloadUrlResponse;
import com.challenge.document.model.Document;
import com.challenge.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("userId") String userId,
            @RequestParam("documentName") String documentName,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestPart("file") MultipartFile file) {
        
        DocumentUploadRequest request = new DocumentUploadRequest();
        request.setUserId(userId);
        request.setDocumentName(documentName);
        
        if (tags != null && !tags.isEmpty()) {
            request.setTags(new HashSet<>(Arrays.asList(tags.split(","))));
        }
        
        Document document = documentService.uploadDocument(request, file);
        return new ResponseEntity<>(document, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<Document>> searchDocuments(
            @RequestParam("userId") String userId,
            @RequestParam(value = "documentName", required = false) String documentName,
            @RequestParam(value = "tag", required = false) String tag,
            @PageableDefault(size = 10) Pageable pageable) {
        
        Page<Document> documents = documentService.searchDocuments(userId, documentName, tag, pageable);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<DownloadUrlResponse> getDownloadUrl(@PathVariable Long documentId) {
        String downloadUrl = documentService.generateDownloadUrl(documentId);
        DownloadUrlResponse response = new DownloadUrlResponse(downloadUrl);
        return ResponseEntity.ok(response);
    }
}