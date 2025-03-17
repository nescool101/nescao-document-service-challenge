package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.RepositoryInterface;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentUploadRequest;
import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentNotFoundException;
import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentUploadException;
import com.clara.ops.challenge.document_management_service_challenge.exception.InvalidFileException;
import com.clara.ops.challenge.document_management_service_challenge.infraestructure.storage.DocumentService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private RepositoryInterface documentRepository;

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService(minioClient, documentRepository);
        // Set bucket name using reflection
        try {
            java.lang.reflect.Field field = DocumentService.class.getDeclaredField("bucketName");
            field.setAccessible(true);
            field.set(documentService, "document-bucket");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void uploadDocument_shouldUploadFileAndSaveMetadata() throws Exception {
        // Arrange
        String userId = "user123";
        String documentName = "test-document.pdf";
        Set<String> tags = new HashSet<>(Arrays.asList("tag1", "tag2"));
        
        DocumentUploadRequest request = new DocumentUploadRequest();
        request.setUserId(userId);
        request.setDocumentName(documentName);
        request.setTags(tags);
        
        byte[] content = "PDF content".getBytes();
        MultipartFile file = new MockMultipartFile(
                "file", 
                "test-document.pdf", 
                "application/pdf", 
                content
        );
        
        Document savedDocument = new Document();
        savedDocument.setId(1L);
        savedDocument.setUserId(userId);
        savedDocument.setDocumentName(documentName);
        savedDocument.setTags(tags);
        
        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);
        
        // Act
        Document result = documentService.uploadDocument(request, file);
        
        // Assert
        verify(minioClient).putObject(any(PutObjectArgs.class));
        
        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(documentCaptor.capture());
        
        Document capturedDocument = documentCaptor.getValue();
        assertThat(capturedDocument.getUserId()).isEqualTo(userId);
        assertThat(capturedDocument.getDocumentName()).isEqualTo(documentName);
        assertThat(capturedDocument.getTags()).isEqualTo(tags);
        assertThat(capturedDocument.getFileSize()).isEqualTo(content.length);
        assertThat(capturedDocument.getFileType()).isEqualTo("application/pdf");
        
        assertThat(result).isEqualTo(savedDocument);
    }
    
    @Test
    void uploadDocument_shouldThrowInvalidFileException_whenFileIsEmpty() {
        // Arrange
        DocumentUploadRequest request = new DocumentUploadRequest();
        request.setUserId("user123");
        request.setDocumentName("test-document.pdf");
        
        MultipartFile file = new MockMultipartFile(
                "file", 
                "test-document.pdf", 
                "application/pdf", 
                new byte[0]
        );
        
        // Act & Assert
        assertThatThrownBy(() -> documentService.uploadDocument(request, file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("File is empty");
        
        verifyNoInteractions(minioClient);
        verifyNoInteractions(documentRepository);
    }
    
    @Test
    void uploadDocument_shouldThrowInvalidFileException_whenFileIsNotPdf() {
        // Arrange
        DocumentUploadRequest request = new DocumentUploadRequest();
        request.setUserId("user123");
        request.setDocumentName("test-document.txt");
        
        MultipartFile file = new MockMultipartFile(
                "file", 
                "test-document.txt", 
                "text/plain", 
                "text content".getBytes()
        );
        
        // Act & Assert
        assertThatThrownBy(() -> documentService.uploadDocument(request, file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("Only PDF files are allowed");
        
        verifyNoInteractions(minioClient);
        verifyNoInteractions(documentRepository);
    }
    
    @Test
    void uploadDocument_shouldThrowDocumentUploadException_whenMinioUploadFails() throws Exception {
        // Arrange
        DocumentUploadRequest request = new DocumentUploadRequest();
        request.setUserId("user123");
        request.setDocumentName("test-document.pdf");
        
        MultipartFile file = new MockMultipartFile(
                "file", 
                "test-document.pdf", 
                "application/pdf", 
                "PDF content".getBytes()
        );
        
        doThrow(new IOException("Upload failed")).when(minioClient).putObject(any(PutObjectArgs.class));
        
        // Act & Assert
        assertThatThrownBy(() -> documentService.uploadDocument(request, file))
                .isInstanceOf(DocumentUploadException.class)
                .hasMessageContaining("Failed to upload document");
        
        verifyNoInteractions(documentRepository);
    }
    
    @Test
    void searchDocuments_shouldReturnAllDocumentsForUser_whenNoFiltersProvided() {
        // Arrange
        String userId = "user123";
        Pageable pageable = PageRequest.of(0, 10);
        
        Document doc1 = new Document();
        doc1.setId(1L);
        doc1.setUserId(userId);
        doc1.setDocumentName("doc1.pdf");
        
        Document doc2 = new Document();
        doc2.setId(2L);
        doc2.setUserId(userId);
        doc2.setDocumentName("doc2.pdf");
        
        List<Document> documents = Arrays.asList(doc1, doc2);
        Page<Document> page = new PageImpl<>(documents, pageable, documents.size());
        
        when(documentRepository.findByUserId(userId, pageable)).thenReturn(page);
        
        // Act
        Page<Document> result = documentService.searchDocuments(userId, null, null, pageable);
        
        // Assert
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(doc1, doc2);
        
        verify(documentRepository).findByUserId(userId, pageable);
        verifyNoMoreInteractions(documentRepository);
    }
    
    @Test
    void searchDocuments_shouldFilterByDocumentName_whenDocumentNameProvided() {
        // Arrange
        String userId = "user123";
        String documentName = "doc";
        Pageable pageable = PageRequest.of(0, 10);
        
        Document doc1 = new Document();
        doc1.setId(1L);
        doc1.setUserId(userId);
        doc1.setDocumentName("doc1.pdf");
        
        List<Document> documents = List.of(doc1);
        Page<Document> page = new PageImpl<>(documents, pageable, documents.size());
        
        when(documentRepository.findByUserIdAndDocumentNameContaining(userId, documentName, pageable))
                .thenReturn(page);
        
        // Act
        Page<Document> result = documentService.searchDocuments(userId, documentName, null, pageable);
        
        // Assert
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(doc1);
        
        verify(documentRepository).findByUserIdAndDocumentNameContaining(userId, documentName, pageable);
        verifyNoMoreInteractions(documentRepository);
    }
    
    @Test
    void searchDocuments_shouldFilterByTag_whenTagProvided() {
        // Arrange
        String userId = "user123";
        String tag = "important";
        Pageable pageable = PageRequest.of(0, 10);
        
        Document doc1 = new Document();
        doc1.setId(1L);
        doc1.setUserId(userId);
        doc1.setDocumentName("doc1.pdf");
        doc1.setTags(new HashSet<>(Arrays.asList("important", "work")));
        
        List<Document> documents = List.of(doc1);
        Page<Document> page = new PageImpl<>(documents, pageable, documents.size());
        
        when(documentRepository.findByUserIdAndTagsContaining(userId, tag, pageable))
                .thenReturn(page);
        
        // Act
        Page<Document> result = documentService.searchDocuments(userId, null, tag, pageable);
        
        // Assert
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(doc1);
        
        verify(documentRepository).findByUserIdAndTagsContaining(userId, tag, pageable);
        verifyNoMoreInteractions(documentRepository);
    }
    
    @Test
    void searchDocuments_shouldFilterByDocumentNameAndTag_whenBothProvided() {
        // Arrange
        String userId = "user123";
        String documentName = "doc";
        String tag = "important";
        Pageable pageable = PageRequest.of(0, 10);
        
        Document doc1 = new Document();
        doc1.setId(1L);
        doc1.setUserId(userId);
        doc1.setDocumentName("doc1.pdf");
        doc1.setTags(new HashSet<>(Arrays.asList("important", "work")));
        
        List<Document> documents = List.of(doc1);
        Page<Document> page = new PageImpl<>(documents, pageable, documents.size());
        
        when(documentRepository.findByUserIdAndDocumentNameContainingAndTagsContaining(
                userId, documentName, tag, pageable))
                .thenReturn(page);
        
        // Act
        Page<Document> result = documentService.searchDocuments(userId, documentName, tag, pageable);
        
        // Assert
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(doc1);
        
        verify(documentRepository).findByUserIdAndDocumentNameContainingAndTagsContaining(
                userId, documentName, tag, pageable);
        verifyNoMoreInteractions(documentRepository);
    }
    
    @Test
    void generateDownloadUrl_shouldReturnPresignedUrl() throws Exception {
        // Arrange
        Long documentId = 1L;
        Document document = new Document();
        document.setId(documentId);
        document.setUserId("user123");
        document.setDocumentName("doc1.pdf");
        document.setMinioPath("user123/doc1.pdf");
        
        String expectedUrl = "https://minio-server/document-bucket/user123/doc1.pdf?token=xyz";
        
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(expectedUrl);
        
        // Act
        String result = documentService.generateDownloadUrl(documentId);
        
        // Assert
        assertThat(result).isEqualTo(expectedUrl);
        
        verify(documentRepository).findById(documentId);
        verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }
    
    @Test
    void generateDownloadUrl_shouldThrowDocumentNotFoundException_whenDocumentNotFound() {
        // Arrange
        Long documentId = 1L;
        
        when(documentRepository.findById(documentId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> documentService.generateDownloadUrl(documentId))
                .isInstanceOf(DocumentNotFoundException.class)
                .hasMessageContaining(String.valueOf(documentId));
        
        verify(documentRepository).findById(documentId);
        verifyNoInteractions(minioClient);
    }
    
    @Test
    void getDocumentById_shouldReturnDocument_whenDocumentExists() {
        // Arrange
        Long documentId = 1L;
        Document document = new Document();
        document.setId(documentId);
        document.setUserId("user123");
        document.setDocumentName("doc1.pdf");
        
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
        
        // Act
        Document result = documentService.getDocumentById(documentId);
        
        // Assert
        assertThat(result).isEqualTo(document);
        
        verify(documentRepository).findById(documentId);
    }
    
    @Test
    void getDocumentById_shouldThrowDocumentNotFoundException_whenDocumentNotFound() {
        // Arrange
        Long documentId = 1L;
        
        when(documentRepository.findById(documentId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> documentService.getDocumentById(documentId))
                .isInstanceOf(DocumentNotFoundException.class)
                .hasMessageContaining(String.valueOf(documentId));
        
        verify(documentRepository).findById(documentId);
    }
    
    @Test
    void deleteDocument_shouldDeleteDocumentFromMinioAndDatabase() throws Exception {
        // Arrange
        Long documentId = 1L;
        Document document = new Document();
        document.setId(documentId);
        document.setUserId("user123");
        document.setDocumentName("doc1.pdf");
        document.setMinioPath("user123/doc1.pdf");
        
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
        
        // Act
        documentService.deleteDocument(documentId);
        
        // Assert
        verify(documentRepository).findById(documentId);
        verify(minioClient).removeObject(any());
        verify(documentRepository).delete(document);
    }
    
    @Test
    void updateDocument_shouldUpdateDocumentMetadata() {
        // Arrange
        Long documentId = 1L;
        Document existingDocument = new Document();
        existingDocument.setId(documentId);
        existingDocument.setUserId("user123");
        existingDocument.setDocumentName("old-name.pdf");
        existingDocument.setTags(new HashSet<>(Arrays.asList("old-tag")));
        
        DocumentUploadRequest request = new DocumentUploadRequest();
        request.setDocumentName("new-name.pdf");
        request.setTags(new HashSet<>(Arrays.asList("new-tag1", "new-tag2")));
        
        Document updatedDocument = new Document();
        updatedDocument.setId(documentId);
        updatedDocument.setUserId("user123");
        updatedDocument.setDocumentName("new-name.pdf");
        updatedDocument.setTags(new HashSet<>(Arrays.asList("new-tag1", "new-tag2")));
        
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(existingDocument));
        when(documentRepository.save(any(Document.class))).thenReturn(updatedDocument);
        
        // Act
        Document result = documentService.updateDocument(documentId, request);
        
        // Assert
        assertThat(result).isEqualTo(updatedDocument);
        
        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(documentCaptor.capture());
        
        Document capturedDocument = documentCaptor.getValue();
        assertThat(capturedDocument.getDocumentName()).isEqualTo("new-name.pdf");
        assertThat(capturedDocument.getTags()).containsExactlyInAnyOrder("new-tag1", "new-tag2");
    }
}