
package com.example.insurance.web.entity;

import com.example.insurance.api.entity.InsuranceProduct;
import com.example.insurance.api.enums.ParseStatus;
import com.example.insurance.common.model.BaseEntity;
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
@Table(name = "document_parse_result")
public class DocumentParseResult extends BaseEntity {

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parse_data", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> parseData;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Column(name = "parse_time")
    private Instant parseTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ParseStatus status = ParseStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", insertable = false, updatable = false)
    private DocumentFile documentFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private InsuranceProduct product;
}