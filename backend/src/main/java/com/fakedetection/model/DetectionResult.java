package com.fakedetection.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "detection_results")
public class DetectionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "social_account_id", nullable = false)
    private SocialAccount socialAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analyzed_by")
    private AppUser analyzedBy;

    @Column(name = "fake_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal fakeScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Verdict verdict;

    @Enumerated(EnumType.STRING)
    @Column(name = "confidence_level", nullable = false, length = 10)
    private ConfidenceLevel confidenceLevel;

    @Column(name = "analysis_notes", columnDefinition = "TEXT")
    private String analysisNotes;

    @Column(name = "analyzed_at", updatable = false)
    private LocalDateTime analyzedAt;

    @Column(name = "model_version", length = 20)
    private String modelVersion = "v1.0";

    @OneToMany(mappedBy = "detectionResult", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FeatureScore> featureScores;

    public enum Verdict { REAL, SUSPICIOUS, FAKE, UNKNOWN }
    public enum ConfidenceLevel { LOW, MEDIUM, HIGH }

    public DetectionResult() {}

    @PrePersist
    protected void onCreate() { analyzedAt = LocalDateTime.now(); }

    // Getters
    public UUID getId() { return id; }
    public SocialAccount getSocialAccount() { return socialAccount; }
    public AppUser getAnalyzedBy() { return analyzedBy; }
    public BigDecimal getFakeScore() { return fakeScore; }
    public Verdict getVerdict() { return verdict; }
    public ConfidenceLevel getConfidenceLevel() { return confidenceLevel; }
    public String getAnalysisNotes() { return analysisNotes; }
    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public String getModelVersion() { return modelVersion; }
    public List<FeatureScore> getFeatureScores() { return featureScores; }

    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setSocialAccount(SocialAccount v) { this.socialAccount = v; }
    public void setAnalyzedBy(AppUser v) { this.analyzedBy = v; }
    public void setFakeScore(BigDecimal v) { this.fakeScore = v; }
    public void setVerdict(Verdict v) { this.verdict = v; }
    public void setConfidenceLevel(ConfidenceLevel v) { this.confidenceLevel = v; }
    public void setAnalysisNotes(String v) { this.analysisNotes = v; }
    public void setModelVersion(String v) { this.modelVersion = v; }
    public void setFeatureScores(List<FeatureScore> v) { this.featureScores = v; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final DetectionResult r = new DetectionResult();
        public Builder socialAccount(SocialAccount v) { r.socialAccount = v; return this; }
        public Builder analyzedBy(AppUser v) { r.analyzedBy = v; return this; }
        public Builder fakeScore(BigDecimal v) { r.fakeScore = v; return this; }
        public Builder verdict(Verdict v) { r.verdict = v; return this; }
        public Builder confidenceLevel(ConfidenceLevel v) { r.confidenceLevel = v; return this; }
        public Builder analysisNotes(String v) { r.analysisNotes = v; return this; }
        public Builder modelVersion(String v) { r.modelVersion = v; return this; }
        public DetectionResult build() { return r; }
    }
}
