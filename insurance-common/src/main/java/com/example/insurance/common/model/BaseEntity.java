
package com.example.insurance.common.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseEntity {

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

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 0;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (deleted == null) {
            deleted = false;
        }
        if (version == null) {
            version = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}