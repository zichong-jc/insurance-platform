
package com.example.insurance.web.entity;

import com.example.insurance.api.entity.InsuranceProduct;
import com.example.insurance.api.entity.InsuranceVersion;
import com.example.insurance.api.enums.DocumentType;
import com.example.insurance.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_file")
public class DocumentFile extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "version_id")
    private Long versionId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "hash", nullable = false, length = 64)
    private String hash;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 20)
    @Builder.Default
    private DocumentType documentType = DocumentType.OTHER;

    @Column(name = "minio_bucket", nullable = false, length = 100)
    private String minioBucket;

    @Column(name = "minio_object_key", nullable = false, length = 500)
    private String minioObjectKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private InsuranceProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", insertable = false, updatable = false)
    private InsuranceVersion insuranceVersion;
}