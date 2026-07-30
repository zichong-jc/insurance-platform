package com.example.insurance.web.entity;

import com.example.insurance.api.entity.InsuranceVersion;
import com.example.insurance.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 保险续保规则
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insurance_renewal_rule")
public class InsuranceRenewalRule extends BaseEntity {

    @Column(name = "version_id", nullable = false)
    private Long versionId;

    /**
     * 续保类型：AUTO-自动续保, MANUAL-手动续保, CONDITIONAL-条件续保
     */
    @Column(name = "renewal_type", nullable = false, length = 50)
    private String renewalType;

    /**
     * 续保规则内容
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", insertable = false, updatable = false)
    private InsuranceVersion insuranceVersion;
}