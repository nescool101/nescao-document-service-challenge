package com.challenge.document.repository;

import com.challenge.document.model.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
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
            
    /**
     * Find a document by user ID and exact document name
     */
    Optional<Document> findByUserIdAndDocumentName(String userId, String documentName);
}