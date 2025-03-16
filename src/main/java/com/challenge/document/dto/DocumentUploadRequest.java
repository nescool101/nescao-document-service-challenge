package com.challenge.document.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {
    private String userId;
    private String documentName;
    private Set<String> tags;
}