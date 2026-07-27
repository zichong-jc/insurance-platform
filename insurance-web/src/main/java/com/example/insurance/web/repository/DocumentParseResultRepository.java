
package com.example.insurance.web.repository;

import com.example.insurance.api.enums.ParseStatus;
import com.example.insurance.web.entity.DocumentParseResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentParseResultRepository extends JpaRepository<DocumentParseResult, Long> {

    Page<DocumentParseResult> findByProductId(Long productId, Pageable pageable);

    Page<DocumentParseResult> findByStatus(ParseStatus status, Pageable pageable);

    Optional<DocumentParseResult> findByFileId(Long fileId);

    List<DocumentParseResult> findByFileIdIn(List<Long> fileIds);

    boolean existsByFileId(Long fileId);
}