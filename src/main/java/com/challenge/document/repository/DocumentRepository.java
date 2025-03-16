package com.challenge.document.repository;

import com.challenge.document.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Document entity.
 * Provides CRUD operations for document metadata in the database.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
}