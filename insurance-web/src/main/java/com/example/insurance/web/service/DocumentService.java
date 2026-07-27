
package com.example.insurance.web.service;

import com.example.insurance.api.dto.response.DocumentFileResponse;
import com.example.insurance.api.dto.response.DocumentParseResultResponse;
import com.example.insurance.api.enums.DocumentType;
import com.example.insurance.api.enums.ParseStatus;
import com.example.insurance.common.exception.ConflictException;
import com.example.insurance.common.exception.ResourceNotFoundException;
import com.example.insurance.common.util.FileUtils;
import com.example.insurance.common.util.HashUtils;
import com.example.insurance.parser.service.ParserService;
import com.example.insurance.web.entity.DocumentFile;
import com.example.insurance.web.entity.DocumentParseResult;
import com.example.insurance.web.mapper.DocumentMapper;
import com.example.insurance.web.repository.DocumentFileRepository;
import com.example.insurance.web.repository.DocumentParseResultRepository;
import com.example.insurance.web.repository.InsuranceProductRepository;
import com.example.insurance.web.repository.InsuranceVersionRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentFileRepository documentFileRepository;
    private final DocumentParseResultRepository parseResultRepository;
    private final InsuranceProductRepository productRepository;
    private final InsuranceVersionRepository versionRepository;
    private final ParserService parserService;
    private final DocumentMapper mapper;
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String minioBucket;

    public Page<DocumentFileResponse> getDocuments(Pageable pageable) {
        log.debug("Getting documents with pagination");
        return documentFileRepository.findAll(pageable)
                .map(this::toDocumentResponse);
    }

    public Page<DocumentFileResponse> getDocumentsByProduct(Long productId, Pageable pageable) {
        log.debug("Getting documents for product: {}", productId);
        return documentFileRepository.findByProductId(productId, pageable)
                .map(this::toDocumentResponse);
    }

    public DocumentFileResponse getDocument(Long id) {
        log.debug("Getting document: {}", id);
        DocumentFile document = documentFileRepository.findById(id)
                .filter(d -> !d.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("DocumentFile", id));
        return toDocumentResponse(document);
    }

    @Transactional
    public DocumentFileResponse uploadDocument(Long productId, Long versionId, 
                                               MultipartFile file, String documentType) {
        log.info("Uploading document for product: {}", productId);
        
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("InsuranceProduct", productId);
        }
        
        if (versionId != null && !versionRepository.existsById(versionId)) {
            throw new ResourceNotFoundException("InsuranceVersion", versionId);
        }
        
        try {
            byte[] content = file.getBytes();
            String hash = HashUtils.sha256(content);
            
            if (documentFileRepository.existsByHash(hash)) {
                throw new ConflictException("Document with same content already exists");
            }
            
            String fileName = FileUtils.generateFileName(file.getOriginalFilename());
            String objectKey = "documents/" + productId + "/" + fileName;
            
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioBucket)
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(FileUtils.getContentType(fileName))
                    .build());
            
            DocumentFile document = DocumentFile.builder()
                    .productId(productId)
                    .versionId(versionId)
                    .fileName(file.getOriginalFilename())
                    .filePath("/documents/" + fileName)
                    .fileSize(file.getSize())
                    .hash(hash)
                    .documentType(DocumentType.fromCode(documentType))
                    .minioBucket(minioBucket)
                    .minioObjectKey(objectKey)
                    .build();
            
            DocumentFile saved = documentFileRepository.save(document);
            log.info("Uploaded document: {} for product: {}", saved.getId(), productId);
            
            return toDocumentResponse(saved);
            
        } catch (ConflictException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to upload document", e);
            throw new RuntimeException("Failed to upload document", e);
        }
    }

    @Transactional
    public DocumentParseResultResponse parseDocument(Long documentId) {
        log.info("Parsing document: {}", documentId);
        
        DocumentFile document = documentFileRepository.findById(documentId)
                .filter(d -> !d.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("DocumentFile", documentId));
        
        DocumentParseResult existingResult = parseResultRepository.findByFileId(documentId).orElse(null);
        
        if (existingResult != null && existingResult.getStatus() == ParseStatus.RUNNING) {
            throw new ConflictException("Document is already being parsed");
        }
        
        try {
            DocumentParseResult parseResult = DocumentParseResult.builder()
                    .fileId(documentId)
                    .productId(document.getProductId())
                    .status(ParseStatus.RUNNING)
                    .build();
            
            Map<String, Object> parseData = parserService.parseWithSummary(
                    new byte[0], 
                    document.getDocumentType().name()
            );
            
            parseResult.setParseData(parseData);
            parseResult.setAiSummary(parseData.get("summary") != null ? parseData.get("summary").toString() : null);
            parseResult.setStatus(ParseStatus.SUCCESS);
            parseResult.setParseTime(Instant.now());
            
            DocumentParseResult saved = parseResultRepository.save(parseResult);
            log.info("Parsed document: {} successfully", documentId);
            
            return toParseResponse(saved, document);
            
        } catch (Exception e) {
            log.error("Failed to parse document", e);
            
            DocumentParseResult parseResult = DocumentParseResult.builder()
                    .fileId(documentId)
                    .productId(document.getProductId())
                    .status(ParseStatus.FAILED)
                    .parseTime(Instant.now())
                    .build();
            parseResultRepository.save(parseResult);
            
            throw new RuntimeException("Failed to parse document", e);
        }
    }

    public DocumentParseResultResponse getParseResult(Long documentId) {
        log.debug("Getting parse result for document: {}", documentId);
        
        DocumentParseResult result = parseResultRepository.findByFileId(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentParseResult", documentId));
        
        DocumentFile document = documentFileRepository.findById(documentId)
                .orElse(null);
        
        return toParseResponse(result, document);
    }

    @Transactional
    public void deleteDocument(Long id) {
        log.info("Deleting document: {}", id);
        
        DocumentFile document = documentFileRepository.findById(id)
                .filter(d -> !d.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("DocumentFile", id));
        
        document.setDeleted(true);
        documentFileRepository.save(document);
        
        log.info("Soft deleted document: {}", id);
    }

    private DocumentFileResponse toDocumentResponse(DocumentFile document) {
        DocumentFileResponse response = mapper.toDocumentResponse(document);
        
        productRepository.findById(document.getProductId())
                .ifPresent(product -> response.setProductName(product.getName()));
        
        if (document.getVersionId() != null) {
            versionRepository.findById(document.getVersionId())
                    .ifPresent(version -> response.setVersionNumber(version.getVersionNumber()));
        }
        
        return response;
    }

    private DocumentParseResultResponse toParseResponse(DocumentParseResult result, DocumentFile document) {
        DocumentParseResultResponse response = mapper.toParseResultResponse(result);
        
        if (document != null) {
            response.setFileName(document.getFileName());
            productRepository.findById(document.getProductId())
                    .ifPresent(product -> response.setProductName(product.getName()));
        }
        
        return response;
    }
}