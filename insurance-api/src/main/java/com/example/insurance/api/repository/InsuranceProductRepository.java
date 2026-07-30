package com.example.insurance.api.repository;

import com.example.insurance.api.entity.InsuranceProduct;
import com.example.insurance.api.enums.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceProductRepository extends JpaRepository<InsuranceProduct, Long> {

    Page<InsuranceProduct> findByCompanyId(Long companyId, Pageable pageable);

    Page<InsuranceProduct> findByType(ProductType type, Pageable pageable);

    List<InsuranceProduct> findByCompanyIdAndStatus(Long companyId, String status);

    Optional<InsuranceProduct> findByCompanyIdAndName(Long companyId, String name);

    Optional<InsuranceProduct> findByProductCode(String productCode);

    boolean existsByCompanyIdAndName(Long companyId, String name);

    boolean existsByProductCode(String productCode);

    @Query("SELECT COUNT(v) FROM InsuranceVersion v WHERE v.productId = :productId")
    Long countVersionsByProductId(@Param("productId") Long productId);
}