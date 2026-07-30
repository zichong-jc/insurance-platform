package com.example.insurance.web.entity;

import com.example.insurance.api.entity.InsuranceVersion;
import com.example.insurance.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 保险保障责任
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insurance_coverage")
public class InsuranceCoverage extends BaseEntity {

    @Column(name = "version_id", nullable = false)
    private Long versionId;

    /**
     * 保障责任名称
     */
    @Column(name = "coverage_name", nullable = false, length = 100)
    private String coverageName;

    /**
     * 保障责任描述
     */
    @Column(name = "coverage_desc", columnDefinition = "TEXT")
    private String coverageDesc;

    /**
     * 保障金额/限额
     */
    @Column(name = "amount", length = 100)
    private String amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", insertable = false, updatable = false)
    private InsuranceVersion insuranceVersion;
}