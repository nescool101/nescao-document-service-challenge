package com.clara.ops.challenge.document_management_service_challenge.infraestructure.rest;

import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.domain.service.DocumentServiceInterface;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentUploadRequest;
import com.clara.ops.challenge.document_management_service_challenge.dto.DownloadUrlResponse;
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

    private final DocumentServiceInterface documentService;

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
    
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long documentId) {
        documentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{documentId}")
    public ResponseEntity<Document> updateDocument(
            @PathVariable Long documentId,
            @RequestBody DocumentUploadRequest request) {
        Document document = documentService.updateDocument(documentId, request);
        return ResponseEntity.ok(document);
    }
}