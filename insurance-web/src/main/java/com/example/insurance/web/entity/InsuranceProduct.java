
package com.example.insurance.web.entity;

import com.example.insurance.api.enums.ProductType;
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
@Table(name = "insurance_product")
public class InsuranceProduct extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private ProductType type = ProductType.OTHER;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private InsuranceCompany company;
}