
package com.example.insurance.sync.repository;

import com.example.insurance.api.enums.SyncStatus;
import com.example.insurance.sync.entity.SyncLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SyncLogRepository extends JpaRepository<SyncLog, Long> {

    Page<SyncLog> findByCompanyId(Long companyId, Pageable pageable);

    Page<SyncLog> findByStatus(SyncStatus status, Pageable pageable);

    List<SyncLog> findByCompanyIdAndProductId(Long companyId, Long productId);

    @Query("SELECT s FROM SyncLog s WHERE s.syncTime >= :startTime")
    List<SyncLog> findBySyncTimeSince(@Param("startTime") Instant startTime);

    @Query("SELECT s FROM SyncLog s WHERE s.companyId = :companyId ORDER BY s.syncTime DESC")
    List<SyncLog> findLatestByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT COUNT(s) FROM SyncLog s WHERE s.status = :status AND s.syncTime >= :startTime")
    Long countByStatusSince(@Param("status") SyncStatus status, @Param("startTime") Instant startTime);
}