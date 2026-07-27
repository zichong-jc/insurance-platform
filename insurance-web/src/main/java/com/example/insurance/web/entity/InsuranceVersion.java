
package com.example.insurance.web.entity;

import com.example.insurance.api.enums.SyncStatus;
import com.example.insurance.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insurance_version")
public class InsuranceVersion extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "version_number", nullable = false, length = 50)
    private String versionNumber;

    @Column(name = "hash", nullable = false, length = 64)
    private String hash;

    @Column(name = "download_url", nullable = false, length = 500)
    private String downloadUrl;

    @Column(name = "sync_time")
    private Instant syncTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SyncStatus status = SyncStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private InsuranceProduct product;
}