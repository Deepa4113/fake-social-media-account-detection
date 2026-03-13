package com.fakedetection.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "feature_scores")
public class FeatureScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detection_result_id", nullable = false)
    private DetectionResult detectionResult;

    @Column(name = "feature_name", nullable = false, length = 100)
    private String featureName;

    @Column(name = "feature_value", precision = 10, scale = 4)
    private BigDecimal featureValue;

    @Column(name = "feature_score", precision = 5, scale = 2)
    private BigDecimal featureScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature_category", length = 50)
    private FeatureCategory featureCategory;

    public enum FeatureCategory { PROFILE, ACTIVITY, NETWORK, CONTENT, BEHAVIORAL }

    public FeatureScore() {}

    // Getters
    public UUID getId() { return id; }
    public DetectionResult getDetectionResult() { return detectionResult; }
    public String getFeatureName() { return featureName; }
    public BigDecimal getFeatureValue() { return featureValue; }
    public BigDecimal getFeatureScore() { return featureScore; }
    public FeatureCategory getFeatureCategory() { return featureCategory; }

    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setDetectionResult(DetectionResult v) { this.detectionResult = v; }
    public void setFeatureName(String v) { this.featureName = v; }
    public void setFeatureValue(BigDecimal v) { this.featureValue = v; }
    public void setFeatureScore(BigDecimal v) { this.featureScore = v; }
    public void setFeatureCategory(FeatureCategory v) { this.featureCategory = v; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final FeatureScore f = new FeatureScore();
        public Builder featureName(String v) { f.featureName = v; return this; }
        public Builder featureValue(BigDecimal v) { f.featureValue = v; return this; }
        public Builder featureScore(BigDecimal v) { f.featureScore = v; return this; }
        public Builder featureCategory(FeatureCategory v) { f.featureCategory = v; return this; }
        public FeatureScore build() { return f; }
    }
}
