package com.clara.ops.challenge.document_management_service_challenge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.domain.service.DocumentServiceInterface;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentUploadRequest;
import com.clara.ops.challenge.document_management_service_challenge.infraestructure.rest.DocumentController;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

  @Mock private DocumentServiceInterface documentService;

  @InjectMocks private DocumentController documentController;

  @Test
  void uploadDocument_shouldReturnCreatedDocument() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "PDF content".getBytes());

    Document document = new Document();
    document.setId(1L);
    document.setUserId("user123");
    document.setDocumentName("test.pdf");
    Set<String> tags = new HashSet<>(Arrays.asList("tag1", "tag2"));
    document.setTags(tags);
    document.setMinioPath("user123/test.pdf");
    document.setFileSize(12L);
    document.setFileType("application/pdf");
    document.setCreatedAt(LocalDateTime.now());

    when(documentService.uploadDocument(any(DocumentUploadRequest.class), eq(file)))
        .thenReturn(document);

    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(documentController).build();

    // Act & Assert
    mockMvc
        .perform(
            multipart("/api/documents")
                .file(file)
                .param("userId", "user123")
                .param("documentName", "test.pdf")
                .param("tags", "tag1,tag2"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.userId").value("user123"))
        .andExpect(jsonPath("$.documentName").value("test.pdf"))
        .andExpect(jsonPath("$.tags").isArray())
        .andExpect(jsonPath("$.fileSize").value(12));
  }

  @Test
  void searchDocuments_shouldReturnPageOfDocuments() throws Exception {
    // Arrange
    Document doc1 = new Document();
    doc1.setId(1L);
    doc1.setUserId("user123");
    doc1.setDocumentName("document1.pdf");
    doc1.setMinioPath("user123/document1.pdf");
    doc1.setFileSize(1000L);
    doc1.setFileType("application/pdf");
    doc1.setCreatedAt(LocalDateTime.now());
    doc1.setTags(new HashSet<>(Arrays.asList("tag1", "tag2")));

    Document doc2 = new Document();
    doc2.setId(2L);
    doc2.setUserId("user123");
    doc2.setDocumentName("document2.pdf");
    doc2.setMinioPath("user123/document2.pdf");
    doc2.setFileSize(2000L);
    doc2.setFileType("application/pdf");
    doc2.setCreatedAt(LocalDateTime.now());
    doc2.setTags(new HashSet<>(Arrays.asList("tag2", "tag3")));

    List<Document> documents = Arrays.asList(doc1, doc2);
    Page<Document> page = new PageImpl<>(documents, PageRequest.of(0, 10), documents.size());

    when(documentService.searchDocuments(eq("user123"), eq(null), eq(null), any(Pageable.class)))
        .thenReturn(page);

    MockMvc mockMvc =
        MockMvcBuilders.standaloneSetup(documentController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .build();

    // Act & Assert
    mockMvc
        .perform(
            get("/api/documents")
                .param("userId", "user123")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void getDownloadUrl_shouldReturnDownloadUrl() throws Exception {
    // Arrange
    String downloadUrl = "https://minio-server/document-bucket/user123/doc1.pdf?token=xyz";

    when(documentService.generateDownloadUrl(1L)).thenReturn(downloadUrl);

    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(documentController).build();

    // Act & Assert
    mockMvc
        .perform(get("/api/documents/1/download"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.downloadUrl").value(downloadUrl));
  }
}
