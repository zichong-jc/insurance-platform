
package com.example.insurance.sync.entity;

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
@Table(name = "sync_log")
public class SyncLog extends BaseEntity {

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SyncStatus status = SyncStatus.PENDING;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "sync_time", nullable = false)
    private Instant syncTime;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "files_downloaded")
    @Builder.Default
    private Integer filesDownloaded = 0;

    @Column(name = "files_skipped")
    @Builder.Default
    private Integer filesSkipped = 0;

    @Column(name = "files_failed")
    @Builder.Default
    private Integer filesFailed = 0;
}