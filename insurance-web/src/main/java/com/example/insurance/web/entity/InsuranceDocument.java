package com.example.insurance.web.entity;

import com.example.insurance.api.entity.InsuranceVersion;
import com.example.insurance.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * 保险文档（条款文档）
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insurance_document")
public class InsuranceDocument extends BaseEntity {

    @Column(name = "version_id", nullable = false)
    private Long versionId;

    /**
     * 文档类型：TERMS-条款, CLAUSE-章节, RIDER-附加险, NOTICE-投保须知
     */
    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    /**
     * 文档名称
     */
    @Column(name = "document_name", nullable = false, length = 200)
    private String documentName;

    /**
     * 文件URL
     */
    @Column(name = "file_url", columnDefinition = "TEXT")
    private String fileUrl;

    /**
     * 文件哈希
     */
    @Column(name = "file_hash", length = 64)
    private String fileHash;

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