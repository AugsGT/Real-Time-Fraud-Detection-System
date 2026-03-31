package com.frauddetection.repository;

import com.frauddetection.model.FraudAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {

    Page<FraudAlert> findBySeverity(FraudAlert.Severity severity, Pageable pageable);

    Page<FraudAlert> findByResolved(Boolean resolved, Pageable pageable);

    Page<FraudAlert> findByResolvedAndSeverity(Boolean resolved, FraudAlert.Severity severity, Pageable pageable);

    Page<FraudAlert> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    long countBySeverity(FraudAlert.Severity severity);
    
    long countByAlertType(FraudAlert.AlertType alertType);

    long countByResolved(Boolean resolved);

    @Query("SELECT COUNT(a) FROM FraudAlert a WHERE a.createdAt >= :since")
    long countSince(@Param("since") LocalDateTime since);

    @Query("SELECT AVG(a.anomalyScore) FROM FraudAlert a WHERE a.anomalyScore IS NOT NULL")
    Double avgAnomalyScore();
}
