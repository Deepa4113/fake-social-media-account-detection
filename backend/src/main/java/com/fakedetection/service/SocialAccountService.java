package com.fakedetection.service;

import com.fakedetection.model.*;
import com.fakedetection.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class SocialAccountService {

    private final SocialAccountRepository accountRepository;
    private final DetectionEngineService detectionEngine;

    @Autowired
    public SocialAccountService(SocialAccountRepository accountRepository, DetectionEngineService detectionEngine) {
        this.accountRepository = accountRepository;
        this.detectionEngine = detectionEngine;
    }

    @Transactional
    public SocialAccount createAccount(SocialAccount account) {
        accountRepository.findByPlatformAndUsername(account.getPlatform(), account.getUsername())
                .ifPresent(existing -> { throw new IllegalArgumentException("Account already exists."); });
        return accountRepository.save(account);
    }

    public Page<SocialAccount> getAllAccounts(int page, int size) {
        return accountRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public SocialAccount getAccountById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Account not found: " + id));
    }

    public Page<SocialAccount> searchAccounts(String query, int page, int size) {
        return accountRepository.searchByUsernameOrDisplayName(query, PageRequest.of(page, size));
    }

    @Transactional
    public DetectionResult analyzeAccount(UUID accountId, AppUser analyst) {
        SocialAccount account = getAccountById(accountId);
        return detectionEngine.analyze(account, analyst);
    }

    @Transactional
    public void deleteAccount(UUID id) {
        accountRepository.deleteById(id);
    }
}
