package com.example.insurance.api.repository;

import com.example.insurance.api.entity.InsuranceCompany;
import com.example.insurance.api.enums.CompanyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceCompanyRepository extends JpaRepository<InsuranceCompany, Long> {

    Optional<InsuranceCompany> findByCode(String code);

    Optional<InsuranceCompany> findByName(String name);

    List<InsuranceCompany> findByType(CompanyType type);

    boolean existsByCode(String code);

    boolean existsByName(String name);
}