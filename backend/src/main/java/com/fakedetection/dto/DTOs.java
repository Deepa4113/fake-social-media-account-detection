package com.fakedetection.dto;

import com.fakedetection.model.DetectionResult;
import com.fakedetection.model.SocialAccount;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DTOs {

    public static class LoginRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public void setUsername(String v) { this.username = v; }
        public void setPassword(String v) { this.password = v; }
    }

    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public void setUsername(String v) { this.username = v; }
        public void setEmail(String v) { this.email = v; }
        public void setPassword(String v) { this.password = v; }
    }

    public static class AuthResponse {
        public String accessToken;
        public String tokenType;
        public Long expiresIn;
        public String username;
        public String role;
    }

    public static class DetectionResponse {
        public UUID id;
        public UUID socialAccountId;
        public String username;
        public String platform;
        public BigDecimal fakeScore;
        public String verdict;
        public String confidenceLevel;
        public String analysisNotes;
        public LocalDateTime analyzedAt;
        public List<FeatureScoreDTO> featureScores;
    }

    public static class FeatureScoreDTO {
        public String featureName;
        public BigDecimal featureValue;
        public BigDecimal featureScore;
        public String featureCategory;
    }

    public static class StatsResponse {
        public long totalFake;
        public long totalReal;
        public long totalSuspicious;
        public long totalAnalyzed;
        public Double avgFakeScore;
    }
}
