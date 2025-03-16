package com.challenge.document.service;

import com.challenge.document.dto.DocumentUploadRequest;
import com.challenge.document.exception.DocumentUploadException;
import com.challenge.document.exception.InvalidFileException;
import com.challenge.document.model.Document;
import com.challenge.document.repository.DocumentRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;


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