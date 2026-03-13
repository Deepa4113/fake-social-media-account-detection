package com.fakedetection.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "social_accounts")
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Platform platform;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(name = "profile_url")
    private String profileUrl;

    @Column(name = "profile_pic_url")
    private String profilePicUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "followers_count")
    private Long followersCount = 0L;

    @Column(name = "following_count")
    private Long followingCount = 0L;

    @Column(name = "posts_count")
    private Long postsCount = 0L;

    @Column(name = "account_age_days")
    private Integer accountAgeDays = 0;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "has_profile_pic")
    private Boolean hasProfilePic = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by")
    private AppUser submittedBy;

    @OneToMany(mappedBy = "socialAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetectionResult> detectionResults;

    public enum Platform { TWITTER, INSTAGRAM, FACEBOOK, TIKTOK, LINKEDIN, OTHER }

    public SocialAccount() {}

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    // Getters
    public UUID getId() { return id; }
    public Platform getPlatform() { return platform; }
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public String getProfileUrl() { return profileUrl; }
    public String getProfilePicUrl() { return profilePicUrl; }
    public String getBio() { return bio; }
    public Long getFollowersCount() { return followersCount; }
    public Long getFollowingCount() { return followingCount; }
    public Long getPostsCount() { return postsCount; }
    public Integer getAccountAgeDays() { return accountAgeDays; }
    public Boolean getIsVerified() { return isVerified; }
    public Boolean getHasProfilePic() { return hasProfilePic; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public AppUser getSubmittedBy() { return submittedBy; }
    public List<DetectionResult> getDetectionResults() { return detectionResults; }

    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setPlatform(Platform v) { this.platform = v; }
    public void setUsername(String v) { this.username = v; }
    public void setDisplayName(String v) { this.displayName = v; }
    public void setProfileUrl(String v) { this.profileUrl = v; }
    public void setProfilePicUrl(String v) { this.profilePicUrl = v; }
    public void setBio(String v) { this.bio = v; }
    public void setFollowersCount(Long v) { this.followersCount = v; }
    public void setFollowingCount(Long v) { this.followingCount = v; }
    public void setPostsCount(Long v) { this.postsCount = v; }
    public void setAccountAgeDays(Integer v) { this.accountAgeDays = v; }
    public void setIsVerified(Boolean v) { this.isVerified = v; }
    public void setHasProfilePic(Boolean v) { this.hasProfilePic = v; }
    public void setSubmittedBy(AppUser v) { this.submittedBy = v; }
    public void setDetectionResults(List<DetectionResult> v) { this.detectionResults = v; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final SocialAccount s = new SocialAccount();
        public Builder platform(Platform v) { s.platform = v; return this; }
        public Builder username(String v) { s.username = v; return this; }
        public Builder displayName(String v) { s.displayName = v; return this; }
        public Builder bio(String v) { s.bio = v; return this; }
        public Builder followersCount(Long v) { s.followersCount = v; return this; }
        public Builder followingCount(Long v) { s.followingCount = v; return this; }
        public Builder postsCount(Long v) { s.postsCount = v; return this; }
        public Builder accountAgeDays(Integer v) { s.accountAgeDays = v; return this; }
        public Builder isVerified(Boolean v) { s.isVerified = v; return this; }
        public Builder hasProfilePic(Boolean v) { s.hasProfilePic = v; return this; }
        public Builder submittedBy(AppUser v) { s.submittedBy = v; return this; }
        public SocialAccount build() { return s; }
    }
}
