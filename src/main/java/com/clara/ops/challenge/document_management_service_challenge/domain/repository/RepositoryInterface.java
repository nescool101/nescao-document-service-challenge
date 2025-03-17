package com.clara.ops.challenge.document_management_service_challenge.domain.repository;

import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Document domain model in hexagonal architecture.
 * This interface is part of the domain layer and defines the contract that
 * infrastructure implementations must fulfill.
 */
public interface RepositoryInterface {
    
    /**
     * Save a document
     */
    Document save(Document document);
    
    /**
     * Find a document by its ID
     */
    Optional<Document> findById(Long id);
    
    /**
     * Delete a document
     */
    void delete(Document document);
    
    /**
     * Find documents by user ID with pagination
     */
    Page<Document> findByUserId(String userId, Pageable pageable);
    
    /**
     * Find documents by user ID and document name containing the given string with pagination
     */
    Page<Document> findByUserIdAndDocumentNameContaining(String userId, String documentName, Pageable pageable);
    
    /**
     * Find documents by user ID and tags containing the given tag with pagination
     */
    Page<Document> findByUserIdAndTagsContaining(String userId, String tag, Pageable pageable);
    
    /**
     * Find documents by user ID, document name containing the given string, and tags containing the given tag with pagination
     */
    Page<Document> findByUserIdAndDocumentNameContainingAndTagsContaining(
            String userId, String documentName, String tag, Pageable pageable);
}