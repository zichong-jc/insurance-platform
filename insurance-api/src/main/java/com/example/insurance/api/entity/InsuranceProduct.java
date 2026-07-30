package com.example.insurance.api.entity;

import com.example.insurance.api.enums.ProductType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insurance_product")
public class InsuranceProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 0;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    /**
     * 产品名称
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * 产品唯一编码（对外标识）
     */
    @Column(name = "product_code", unique = true, length = 100)
    private String productCode;

    /**
     * 产品类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private ProductType type = ProductType.OTHER;

    /**
     * 产品状态
     */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    /**
     * 产品描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 产品属性定义（物模型属性）
     * 如：保障期限、投保年龄、保额范围等
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "properties", columnDefinition = "jsonb")
    private Map<String, Object> properties;

    /**
     * 产品服务定义（物模型服务）
     * 如：理赔服务、续保服务、退保服务等
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "services", columnDefinition = "jsonb")
    private Map<String, Object> services;

    /**
     * 当前生效版本ID
     */
    @Column(name = "current_version_id")
    private Long currentVersionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private InsuranceCompany company;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}