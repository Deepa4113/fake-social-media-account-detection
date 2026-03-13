package com.fakedetection.repository;

import com.fakedetection.model.FeatureScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeatureScoreRepository extends JpaRepository<FeatureScore, UUID> {
    List<FeatureScore> findByDetectionResultId(UUID detectionResultId);
}
