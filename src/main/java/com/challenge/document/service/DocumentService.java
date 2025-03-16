package com.challenge.document.service;

import com.challenge.document.dto.DocumentUploadRequest;
import com.challenge.document.exception.DocumentNotFoundException;
import com.challenge.document.exception.DocumentUploadException;
import com.challenge.document.exception.InvalidFileException;
import com.challenge.document.model.Document;
import com.challenge.document.repository.DocumentRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;


/**
 * Service class handling document management operations.
 * Manages document uploads, storage in MinIO, and metadata persistence.
 */
@Service
@Slf4j
public class DocumentService {
    private final MinioClient minioClient;
    private final DocumentRepository documentRepository;
    
    @Value("${minio.bucket.name}")
    private String bucketName;

    public DocumentService(MinioClient minioClient, DocumentRepository documentRepository) {
        this.minioClient = minioClient;
        this.documentRepository = documentRepository;
    }

    /**
     * Uploads a document to MinIO and saves its metadata to the database.
     *
     * @param request The metadata for the document
     * @param file The PDF file to upload
     * @return The saved document metadata
     * @throws DocumentUploadException if the upload to MinIO fails
     * @throws InvalidFileException if the file validation fails
     */
    public Document uploadDocument(DocumentUploadRequest request, MultipartFile file) {
        validateFile(file);
        
        String minioPath = generateMinioPath(request.getUserId(), request.getDocumentName());
        
        try {
            // Upload to MinIO using streaming to handle large files
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(minioPath)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType("application/pdf")
                    .build()
            );
            
            // Create document record
            Document document = new Document();
            document.setUserId(request.getUserId());
            document.setDocumentName(request.getDocumentName());
            document.setTags(request.getTags());
            document.setMinioPath(minioPath);
            document.setFileSize(file.getSize());
            document.setFileType("application/pdf");
            document.setCreatedAt(LocalDateTime.now());
            
            return documentRepository.save(document);
        } catch (Exception e) {
            log.error("Error uploading document", e);
            throw new DocumentUploadException("Failed to upload document", e);
        }
    }
    
    /**
     * Searches for documents based on the provided criteria.
     *
     * @param userId The ID of the user whose documents to search
     * @param documentName Optional document name to filter by
     * @param tag Optional tag to filter by
     * @param pageable Pagination information
     * @return A page of documents matching the criteria
     */
    public Page<Document> searchDocuments(String userId, String documentName, String tag, Pageable pageable) {
        // This is a simplified implementation. In a real application, you might want to use
        // a more sophisticated search mechanism like Spring Data JPA Specifications or QueryDSL
        if (documentName != null && tag != null) {
            return documentRepository.findByUserIdAndDocumentNameContainingAndTagsContaining(
                    userId, documentName, tag, pageable);
        } else if (documentName != null) {
            return documentRepository.findByUserIdAndDocumentNameContaining(userId, documentName, pageable);
        } else if (tag != null) {
            return documentRepository.findByUserIdAndTagsContaining(userId, tag, pageable);
        } else {
            return documentRepository.findByUserId(userId, pageable);
        }
    }
    
    /**
     * Generates a pre-signed URL for downloading a document.
     *
     * @param documentId The ID of the document to download
     * @return A pre-signed URL for downloading the document
     * @throws DocumentNotFoundException if the document is not found
     * @throws RuntimeException if URL generation fails
     */
    public String generateDownloadUrl(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
        
        try {
            // Generate a pre-signed URL that expires after 1 hour
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(document.getMinioPath())
                    .method(Method.GET)
                    .expiry(1, TimeUnit.HOURS)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error generating download URL for document ID: {}", documentId, e);
            throw new RuntimeException("Failed to generate download URL", e);
        }
    }
    
    /**
     * Validates the uploaded file.
     *
     * @param file The file to validate
     * @throws InvalidFileException if the file is empty or not a PDF
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }
        if (!file.getContentType().equals("application/pdf")) {
            throw new InvalidFileException("Only PDF files are allowed");
        }
    }
    
    /**
     * Generates the MinIO path for storing the document.
     *
     * @param userId The ID of the user uploading the document
     * @param documentName The name of the document
     * @return The generated MinIO path
     */
    private String generateMinioPath(String userId, String documentName) {
        return String.format("%s/%s", userId, documentName);
    }
}