
package com.example.insurance.web.entity;

import com.example.insurance.api.enums.CompanyType;
import com.example.insurance.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insurance_company")
public class InsuranceCompany extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private CompanyType type = CompanyType.OTHER;

    @Column(name = "website", length = 255)
    private String website;
}