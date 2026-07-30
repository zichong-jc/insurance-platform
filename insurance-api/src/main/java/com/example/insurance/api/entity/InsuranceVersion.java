package com.example.insurance.api.entity;

import com.example.insurance.api.enums.SyncStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 保险产品版本（版本管理）
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insurance_version")
public class InsuranceVersion {

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

    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * 版本号（语义化版本，如：1.0.0, 1.1.0, 2.0.0）
     */
    @Column(name = "version_number", nullable = false, length = 50)
    private String versionNumber;

    /**
     * 版本类型：MAJOR-主版本, MINOR-次版本, PATCH-补丁
     */
    @Column(name = "version_type", length = 10)
    private String versionType;

    /**
     * 版本说明
     */
    @Column(name = "version_description", columnDefinition = "TEXT")
    private String versionDescription;

    /**
     * 版本生效日期
     */
    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    /**
     * 版本失效日期
     */
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    /**
     * 版本状态：DRAFT-草稿, ACTIVE-生效, SUPERSEDED-已替代, ARCHIVED-已归档
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SyncStatus status = SyncStatus.PENDING;

    /**
     * 上一个版本ID（版本链）
     */
    @Column(name = "previous_version_id")
    private Long previousVersionId;

    /**
     * 同步时间
     */
    @Column(name = "sync_time")
    private Instant syncTime;

    /**
     * 条款文档哈希值（用于检测变更）
     */
    @Column(name = "hash", length = 64)
    private String hash;

    /**
     * 条款下载URL
     */
    @Column(name = "download_url", length = 500)
    private String downloadUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private InsuranceProduct product;

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