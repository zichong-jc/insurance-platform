package com.example.insurance.web.entity;

import com.example.insurance.api.entity.InsuranceVersion;
import com.example.insurance.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 保险免责条款
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insurance_exclusion")
public class InsuranceExclusion extends BaseEntity {

    @Column(name = "version_id", nullable = false)
    private Long versionId;

    /**
     * 免责条款名称
     */
    @Column(name = "exclusion_name", nullable = false, length = 100)
    private String exclusionName;

    /**
     * 免责内容
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", insertable = false, updatable = false)
    private InsuranceVersion insuranceVersion;
}