package com.fakedetection.service;

import com.fakedetection.model.*;
import com.fakedetection.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class DetectionEngineService {

    private static final Logger log = LoggerFactory.getLogger(DetectionEngineService.class);

    private final DetectionResultRepository detectionResultRepository;
    private final FeatureScoreRepository featureScoreRepository;

    @Autowired
    public DetectionEngineService(DetectionResultRepository detectionResultRepository,
                                   FeatureScoreRepository featureScoreRepository) {
        this.detectionResultRepository = detectionResultRepository;
        this.featureScoreRepository = featureScoreRepository;
    }

    @Transactional
    public DetectionResult analyze(SocialAccount account, AppUser analyst) {
        log.info("Analyzing account: {} on {}", account.getUsername(), account.getPlatform());

        List<FeatureScore> scores = new ArrayList<>();
        scores.add(scoreProfilePicture(account));
        scores.add(scoreBioQuality(account));
        scores.add(scoreVerificationStatus(account));
        scores.add(scoreFollowerToFollowingRatio(account));
        scores.add(scoreFollowerCount(account));
        scores.add(scoreAccountAge(account));
        scores.add(scorePostsPerDay(account));
        scores.add(scoreSuspiciousKeywords(account));

        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal weightedSum = BigDecimal.ZERO;

        for (FeatureScore fs : scores) {
            BigDecimal weight = getWeight(fs.getFeatureCategory());
            weightedSum = weightedSum.add(fs.getFeatureScore().multiply(weight));
            totalWeight = totalWeight.add(weight);
        }

        BigDecimal fakeScore = totalWeight.compareTo(BigDecimal.ZERO) > 0
                ? weightedSum.divide(totalWeight, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        DetectionResult.Verdict verdict = classifyVerdict(fakeScore);
        DetectionResult.ConfidenceLevel confidence = classifyConfidence(scores);

        DetectionResult result = DetectionResult.builder()
                .socialAccount(account)
                .analyzedBy(analyst)
                .fakeScore(fakeScore)
                .verdict(verdict)
                .confidenceLevel(confidence)
                .analysisNotes(buildNotes(scores, fakeScore))
                .modelVersion("v1.0")
                .build();

        DetectionResult saved = detectionResultRepository.save(result);

        scores.forEach(fs -> {
            fs.setDetectionResult(saved);
            featureScoreRepository.save(fs);
        });

        log.info("Analysis complete for {}: score={}, verdict={}", account.getUsername(), fakeScore, verdict);
        return saved;
    }

    private FeatureScore scoreProfilePicture(SocialAccount acc) {
        BigDecimal score = acc.getHasProfilePic() == null || !acc.getHasProfilePic()
                ? BigDecimal.valueOf(80) : BigDecimal.valueOf(5);
        return buildFeature("has_profile_pic", acc.getHasProfilePic() != null && acc.getHasProfilePic() ? 1.0 : 0.0,
                score, FeatureScore.FeatureCategory.PROFILE);
    }

    private FeatureScore scoreBioQuality(SocialAccount acc) {
        String bio = acc.getBio();
        BigDecimal score;
        if (bio == null || bio.isBlank()) score = BigDecimal.valueOf(60);
        else if (bio.length() < 10) score = BigDecimal.valueOf(40);
        else score = BigDecimal.valueOf(10);
        return buildFeature("bio_quality", bio != null ? (double) bio.length() : 0.0, score, FeatureScore.FeatureCategory.PROFILE);
    }

    private FeatureScore scoreVerificationStatus(SocialAccount acc) {
        BigDecimal score = acc.getIsVerified() != null && acc.getIsVerified()
                ? BigDecimal.valueOf(5) : BigDecimal.valueOf(30);
        return buildFeature("is_verified", acc.getIsVerified() != null && acc.getIsVerified() ? 1.0 : 0.0,
                score, FeatureScore.FeatureCategory.PROFILE);
    }

    private FeatureScore scoreFollowerToFollowingRatio(SocialAccount acc) {
        long followers = acc.getFollowersCount() != null ? acc.getFollowersCount() : 0L;
        long following = acc.getFollowingCount() != null ? acc.getFollowingCount() : 1L;
        if (following == 0) following = 1;
        double ratio = (double) followers / following;
        BigDecimal score;
        if (ratio < 0.1) score = BigDecimal.valueOf(85);
        else if (ratio > 100) score = BigDecimal.valueOf(15);
        else if (ratio >= 0.5 && ratio <= 10) score = BigDecimal.valueOf(10);
        else score = BigDecimal.valueOf(45);
        return buildFeature("follower_following_ratio", ratio, score, FeatureScore.FeatureCategory.NETWORK);
    }

    private FeatureScore scoreFollowerCount(SocialAccount acc) {
        long followers = acc.getFollowersCount() != null ? acc.getFollowersCount() : 0L;
        BigDecimal score;
        if (followers < 10) score = BigDecimal.valueOf(70);
        else if (followers > 10000) score = BigDecimal.valueOf(20);
        else score = BigDecimal.valueOf(30);
        return buildFeature("follower_count", (double) followers, score, FeatureScore.FeatureCategory.NETWORK);
    }

    private FeatureScore scoreAccountAge(SocialAccount acc) {
        int age = acc.getAccountAgeDays() != null ? acc.getAccountAgeDays() : 0;
        BigDecimal score;
        if (age < 7) score = BigDecimal.valueOf(90);
        else if (age < 30) score = BigDecimal.valueOf(60);
        else if (age < 180) score = BigDecimal.valueOf(35);
        else score = BigDecimal.valueOf(10);
        return buildFeature("account_age_days", (double) age, score, FeatureScore.FeatureCategory.ACTIVITY);
    }

    private FeatureScore scorePostsPerDay(SocialAccount acc) {
        int age = acc.getAccountAgeDays() != null && acc.getAccountAgeDays() > 0 ? acc.getAccountAgeDays() : 1;
        long posts = acc.getPostsCount() != null ? acc.getPostsCount() : 0L;
        double postsPerDay = (double) posts / age;
        BigDecimal score;
        if (postsPerDay > 50) score = BigDecimal.valueOf(90);
        else if (postsPerDay > 20) score = BigDecimal.valueOf(65);
        else if (postsPerDay < 0.1) score = BigDecimal.valueOf(55);
        else score = BigDecimal.valueOf(15);
        return buildFeature("posts_per_day", postsPerDay, score, FeatureScore.FeatureCategory.ACTIVITY);
    }

    private FeatureScore scoreSuspiciousKeywords(SocialAccount acc) {
        String[] suspiciousWords = {"click here", "free money", "giveaway", "win now", "dm me", "crypto", "investment", "!!"};
        String combined = ((acc.getBio() != null ? acc.getBio() : "") + " " +
                (acc.getDisplayName() != null ? acc.getDisplayName() : "")).toLowerCase();
        long matches = Arrays.stream(suspiciousWords).filter(combined::contains).count();
        BigDecimal score = BigDecimal.valueOf(Math.min(95, matches * 20));
        return buildFeature("suspicious_keywords", (double) matches, score, FeatureScore.FeatureCategory.CONTENT);
    }

    private FeatureScore buildFeature(String name, double value, BigDecimal score, FeatureScore.FeatureCategory cat) {
        return FeatureScore.builder()
                .featureName(name)
                .featureValue(BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP))
                .featureScore(score)
                .featureCategory(cat)
                .build();
    }

    private BigDecimal getWeight(FeatureScore.FeatureCategory category) {
        return switch (category) {
            case PROFILE    -> BigDecimal.valueOf(1.5);
            case NETWORK    -> BigDecimal.valueOf(2.0);
            case ACTIVITY   -> BigDecimal.valueOf(2.0);
            case CONTENT    -> BigDecimal.valueOf(1.8);
            case BEHAVIORAL -> BigDecimal.valueOf(1.2);
        };
    }

    private DetectionResult.Verdict classifyVerdict(BigDecimal score) {
        double d = score.doubleValue();
        if (d >= 70) return DetectionResult.Verdict.FAKE;
        if (d >= 40) return DetectionResult.Verdict.SUSPICIOUS;
        return DetectionResult.Verdict.REAL;
    }

    private DetectionResult.ConfidenceLevel classifyConfidence(List<FeatureScore> scores) {
        long high = scores.stream()
                .filter(fs -> fs.getFeatureScore().doubleValue() > 70 || fs.getFeatureScore().doubleValue() < 20)
                .count();
        if (high >= 5) return DetectionResult.ConfidenceLevel.HIGH;
        if (high >= 3) return DetectionResult.ConfidenceLevel.MEDIUM;
        return DetectionResult.ConfidenceLevel.LOW;
    }

    private String buildNotes(List<FeatureScore> scores, BigDecimal fakeScore) {
        StringBuilder sb = new StringBuilder("Fake score: ").append(fakeScore).append("/100. ");
        scores.stream().filter(fs -> fs.getFeatureScore().doubleValue() > 60)
                .forEach(fs -> sb.append("High risk: ").append(fs.getFeatureName()).append(". "));
        return sb.toString().trim();
    }
}
