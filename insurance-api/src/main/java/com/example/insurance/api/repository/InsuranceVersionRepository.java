package com.example.insurance.api.repository;

import com.example.insurance.api.entity.InsuranceVersion;
import com.example.insurance.api.enums.VersionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceVersionRepository extends JpaRepository<InsuranceVersion, Long> {

    Page<InsuranceVersion> findByProductId(Long productId, Pageable pageable);

    List<InsuranceVersion> findByProductIdOrderByCreatedAtDesc(Long productId);

    Optional<InsuranceVersion> findByProductIdAndVersionNumber(Long productId, String versionNumber);

    Optional<InsuranceVersion> findByProductIdAndHash(Long productId, String hash);

    boolean existsByProductIdAndHash(Long productId, String hash);

    Page<InsuranceVersion> findByStatus(VersionStatus status, Pageable pageable);
}