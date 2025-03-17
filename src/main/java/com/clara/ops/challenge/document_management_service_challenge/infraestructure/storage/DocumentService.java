package com.clara.ops.challenge.document_management_service_challenge.infraestructure.storage;

import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.RepositoryInterface;
import com.clara.ops.challenge.document_management_service_challenge.domain.service.DocumentServiceInterface;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentUploadRequest;
import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentNotFoundException;
import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentUploadException;
import com.clara.ops.challenge.document_management_service_challenge.exception.InvalidFileException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of DocumentServiceInterface that handles document management operations. Manages
 * document uploads, storage in MinIO, and metadata persistence.
 */
@Service
@Slf4j
public class DocumentService implements DocumentServiceInterface {
  public static final String CONTENT_TYPE_PDF = "application/pdf";
  private final MinioClient minioClient;
  private final RepositoryInterface documentRepository;
  
  // Buffer size for streaming uploads (8KB chunks)
  private static final int BUFFER_SIZE = 8 * 1024;

  @Value("${minio.bucket.name}")
  private String bucketName;

  public DocumentService(MinioClient minioClient, RepositoryInterface documentRepository) {
    this.minioClient = minioClient;
    this.documentRepository = documentRepository;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public Document uploadDocument(DocumentUploadRequest request, MultipartFile file) {
    validateFile(file);

    String minioPath = generateMinioPath(request.getUserId(), request.getDocumentName());

    try (InputStream fileInputStream = new BufferedInputStream(file.getInputStream(), BUFFER_SIZE)) {
      // Use streaming to handle large files without loading them entirely into memory
      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(bucketName)
              .object(minioPath)
              .stream(fileInputStream, file.getSize(), -1)
              .contentType(CONTENT_TYPE_PDF)
              .build());

      Document document = new Document();
      document.setUserId(request.getUserId());
      document.setDocumentName(request.getDocumentName());
      document.setTags(request.getTags());
      document.setMinioPath(minioPath);
      document.setFileSize(file.getSize());
      document.setFileType(CONTENT_TYPE_PDF);
      document.setCreatedAt(LocalDateTime.now());

      return documentRepository.save(document);
    } catch (IOException e) {
      log.error("Error reading file stream", e);
      throw new DocumentUploadException("Failed to read file stream", e);
    } catch (Exception e) {
      log.error("Error uploading document", e);
      throw new DocumentUploadException("Failed to upload document", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Page<Document> searchDocuments(
      String userId, String documentName, String tag, Pageable pageable) {
    if (documentName != null && tag != null) {
      return documentRepository.findByUserIdAndDocumentNameContainingAndTagsContaining(
          userId, documentName, tag, pageable);
    } else if (documentName != null) {
      return documentRepository.findByUserIdAndDocumentNameContaining(
          userId, documentName, pageable);
    } else if (tag != null) {
      return documentRepository.findByUserIdAndTagsContaining(userId, tag, pageable);
    } else {
      return documentRepository.findByUserId(userId, pageable);
    }
  }

  /** {@inheritDoc} */
  @Override
  public String generateDownloadUrl(Long documentId) {
    Document document = getDocumentById(documentId);

    try {
      return minioClient.getPresignedObjectUrl(
          GetPresignedObjectUrlArgs.builder()
              .bucket(bucketName)
              .object(document.getMinioPath())
              .method(Method.GET)
              .expiry(1, TimeUnit.HOURS)
              .build());
    } catch (Exception e) {
      log.error("Error generating download URL for document ID: {}", documentId, e);
      throw new RuntimeException("Failed to generate download URL", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Document getDocumentById(Long documentId) {
    return documentRepository
        .findById(documentId)
        .orElseThrow(() -> new DocumentNotFoundException(documentId));
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void deleteDocument(Long documentId) {
    Document document = getDocumentById(documentId);

    try {
      minioClient.removeObject(
          RemoveObjectArgs.builder().bucket(bucketName).object(document.getMinioPath()).build());

      documentRepository.delete(document);
    } catch (Exception e) {
      log.error("Error deleting document with ID: {}", documentId, e);
      throw new RuntimeException("Failed to delete document", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public Document updateDocument(Long documentId, DocumentUploadRequest request) {
    Document document = getDocumentById(documentId);

    document.setDocumentName(request.getDocumentName());
    document.setTags(request.getTags());

    return documentRepository.save(document);
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
    if (!file.getContentType().equals(CONTENT_TYPE_PDF)) {
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
