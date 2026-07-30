package com.example.insurance.web.entity;

import com.example.insurance.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

/**
 * 保险文档分块（用于RAG向量检索）
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insurance_document_chunk")
public class InsuranceDocumentChunk extends BaseEntity {

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    /**
     * 页码
     */
    @Column(name = "page_no")
    private Integer pageNo;

    /**
     * 分块内容
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 向量嵌入（用于语义搜索）
     * PostgreSQL vector类型
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private float[] embedding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", insertable = false, updatable = false)
    private InsuranceDocument insuranceDocument;
}