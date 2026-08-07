package com.example.insurance.api.entity;

import com.example.insurance.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * 保险文档（条款文档）
 * 关联关系：InsuranceDocument -> InsuranceVersion -> InsuranceProduct
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insurance_document")
public class InsuranceDocument extends BaseEntity {

    /**
     * 版本ID（关联InsuranceVersion）
     */
    @Column(name = "version_id", nullable = false)
    private Long versionId;

    /**
     * 产品ID（直接关联InsuranceProduct，用于快速查询）
     */
    @Column(name = "product_id")
    private Long productId;

    /**
     * 文档类型：TERMS-条款, CLAUSE-章节, RIDER-附加险, NOTICE-投保须知, 
     * COVERAGE-保障责任, EXCLUSION-免责条款, UNDERWRITING-投保规则, RENEWAL-续保规则
     */
    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    /**
     * 文档名称
     */
    @Column(name = "document_name", nullable = false, length = 200)
    private String documentName;

    /**
     * 文件URL（原始URL）
     */
    @Column(name = "file_url", columnDefinition = "TEXT")
    private String fileUrl;

    /**
     * 本地存储路径
     */
    @Column(name = "local_path", columnDefinition = "TEXT")
    private String localPath;

    /**
     * 文件哈希
     */
    @Column(name = "file_hash", length = 64)
    private String fileHash;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 解析状态：PENDING-待解析, PARSING-解析中, COMPLETED-已完成, FAILED-失败
     */
    @Column(name = "parse_status", length = 20)
    private String parseStatus;

    /**
     * 创建时间
     */
    @Column(name = "created_time")
    private Instant createdTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", insertable = false, updatable = false)
    private InsuranceVersion insuranceVersion;
}