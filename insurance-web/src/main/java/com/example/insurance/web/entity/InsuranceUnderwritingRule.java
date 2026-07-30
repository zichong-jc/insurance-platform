package com.example.insurance.web.entity;

import com.example.insurance.api.entity.InsuranceVersion;
import com.example.insurance.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 保险投保规则
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insurance_underwriting_rule")
public class InsuranceUnderwritingRule extends BaseEntity {

    @Column(name = "version_id", nullable = false)
    private Long versionId;

    /**
     * 规则类型：AGE-年龄限制, HEALTH-健康告知, OCCUPATION-职业限制, AMOUNT-保额限制
     */
    @Column(name = "rule_type", nullable = false, length = 50)
    private String ruleType;

    /**
     * 规则值/内容
     */
    @Column(name = "rule_value", columnDefinition = "TEXT")
    private String ruleValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", insertable = false, updatable = false)
    private InsuranceVersion insuranceVersion;
}