package com.fakedetection.repository;

import com.fakedetection.model.DetectionResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DetectionResultRepository extends JpaRepository<DetectionResult, UUID> {

    List<DetectionResult> findBySocialAccountIdOrderByAnalyzedAtDesc(UUID socialAccountId);

    Optional<DetectionResult> findTopBySocialAccountIdOrderByAnalyzedAtDesc(UUID socialAccountId);

    Page<DetectionResult> findByVerdict(DetectionResult.Verdict verdict, Pageable pageable);

    @Query("SELECT COUNT(dr) FROM DetectionResult dr WHERE dr.verdict = :verdict")
    long countByVerdict(DetectionResult.Verdict verdict);

    @Query("SELECT AVG(dr.fakeScore) FROM DetectionResult dr")
    Double averageFakeScore();

    @Query("SELECT dr.verdict, COUNT(dr) FROM DetectionResult dr GROUP BY dr.verdict")
    List<Object[]> countGroupedByVerdict();
}
