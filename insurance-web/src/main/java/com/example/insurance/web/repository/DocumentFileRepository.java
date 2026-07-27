
package com.example.insurance.web.repository;

import com.example.insurance.api.enums.DocumentType;
import com.example.insurance.web.entity.DocumentFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentFileRepository extends JpaRepository<DocumentFile, Long> {

    Page<DocumentFile> findByProductId(Long productId, Pageable pageable);

    Page<DocumentFile> findByDocumentType(DocumentType documentType, Pageable pageable);

    List<DocumentFile> findByVersionId(Long versionId);

    Optional<DocumentFile> findByHash(String hash);

    boolean existsByHash(String hash);

    Page<DocumentFile> findByProductIdAndDocumentType(Long productId, DocumentType documentType, Pageable pageable);
}