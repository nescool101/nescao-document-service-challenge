package com.clara.ops.challenge.document_management_service_challenge.domain.service;

import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentUploadRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for document management operations in the domain layer. Defines the contract
 * that implementations must fulfill.
 */
public interface DocumentServiceInterface {

  /**
   * Uploads a document and saves its metadata.
   *
   * @param request The metadata for the document
   * @param file The file to upload
   * @return The saved document metadata
   */
  Document uploadDocument(DocumentUploadRequest request, MultipartFile file);

  /**
   * Searches for documents based on the provided criteria.
   *
   * @param userId The ID of the user whose documents to search
   * @param documentName Optional document name to filter by
   * @param tag Optional tag to filter by
   * @param pageable Pagination information
   * @return A page of documents matching the criteria
   */
  Page<Document> searchDocuments(String userId, String documentName, String tag, Pageable pageable);

  /**
   * Generates a URL for downloading a document.
   *
   * @param documentId The ID of the document to download
   * @return A URL for downloading the document
   */
  String generateDownloadUrl(Long documentId);

  /**
   * Retrieves a document by its ID.
   *
   * @param documentId The ID of the document to retrieve
   * @return The document if found
   */
  Document getDocumentById(Long documentId);

  /**
   * Deletes a document by its ID.
   *
   * @param documentId The ID of the document to delete
   */
  void deleteDocument(Long documentId);

  /**
   * Updates document metadata.
   *
   * @param documentId The ID of the document to update
   * @param request The updated metadata
   * @return The updated document
   */
  Document updateDocument(Long documentId, DocumentUploadRequest request);
}
