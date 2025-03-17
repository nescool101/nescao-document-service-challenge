package com.clara.ops.challenge.document_management_service_challenge.infraestructure.persistence;

import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.RepositoryInterface;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA implementation of the Document repository interface. This is part of the infrastructure layer
 * in hexagonal architecture.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, RepositoryInterface {

  /** Find documents by user ID with pagination */
  @Override
  Page<Document> findByUserId(String userId, Pageable pageable);

  /** Find documents by user ID and document name containing the given string with pagination */
  @Override
  Page<Document> findByUserIdAndDocumentNameContaining(
      String userId, String documentName, Pageable pageable);

  /** Find documents by user ID and tags containing the given tag with pagination */
  @Override
  Page<Document> findByUserIdAndTagsContaining(String userId, String tag, Pageable pageable);

  /**
   * Find documents by user ID, document name containing the given string, and tags containing the
   * given tag with pagination
   */
  @Override
  Page<Document> findByUserIdAndDocumentNameContainingAndTagsContaining(
      String userId, String documentName, String tag, Pageable pageable);
}
