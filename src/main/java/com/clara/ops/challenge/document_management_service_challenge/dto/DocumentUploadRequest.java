package com.clara.ops.challenge.document_management_service_challenge.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for document upload requests. Contains the metadata information
 * required when uploading a new document.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {
  private String userId;
  private String documentName;
  private Set<String> tags;
}
