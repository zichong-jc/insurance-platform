package com.example.insurance.api.repository;

import com.example.insurance.api.entity.InsuranceDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceDocumentRepository extends JpaRepository<InsuranceDocument, Long> {

    List<InsuranceDocument> findByProductId(Long productId);

    List<InsuranceDocument> findByVersionId(Long versionId);

    Optional<InsuranceDocument> findByProductIdAndDocumentName(Long productId, String documentName);

    boolean existsByProductIdAndFileUrl(Long productId, String fileUrl);

    List<InsuranceDocument> findByParseStatus(String parseStatus);
}