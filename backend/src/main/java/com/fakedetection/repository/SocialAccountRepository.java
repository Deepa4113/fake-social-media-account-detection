package com.fakedetection.repository;

import com.fakedetection.model.SocialAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, UUID> {

    Optional<SocialAccount> findByPlatformAndUsername(SocialAccount.Platform platform, String username);

    Page<SocialAccount> findByPlatform(SocialAccount.Platform platform, Pageable pageable);

    @Query("SELECT sa FROM SocialAccount sa WHERE " +
           "LOWER(sa.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(sa.displayName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<SocialAccount> searchByUsernameOrDisplayName(@Param("query") String query, Pageable pageable);

    @Query("SELECT COUNT(sa) FROM SocialAccount sa WHERE sa.platform = :platform")
    long countByPlatform(@Param("platform") SocialAccount.Platform platform);
}
