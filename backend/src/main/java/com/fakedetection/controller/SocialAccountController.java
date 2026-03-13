package com.fakedetection.controller;

import com.fakedetection.model.*;
import com.fakedetection.repository.DetectionResultRepository;
import com.fakedetection.service.SocialAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Social Accounts", description = "Manage social accounts for analysis")
public class SocialAccountController {

    private final SocialAccountService accountService;
    private final DetectionResultRepository detectionRepo;

    @PostMapping
    @Operation(summary = "Submit a social account for analysis")
    public ResponseEntity<SocialAccount> createAccount(
            @RequestBody SocialAccount account,
            @AuthenticationPrincipal AppUser currentUser) {
        account.setSubmittedBy(currentUser);
        SocialAccount saved = accountService.createAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    @Operation(summary = "List all submitted accounts (paginated)")
    public ResponseEntity<Page<SocialAccount>> getAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(accountService.searchAccounts(search, page, size));
        }
        return ResponseEntity.ok(accountService.getAllAccounts(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account details by ID")
    public ResponseEntity<SocialAccount> getAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @PostMapping("/{id}/analyze")
    @Operation(summary = "Trigger detection analysis for an account")
    public ResponseEntity<DetectionResult> analyzeAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUser currentUser) {
        DetectionResult result = accountService.analyzeAccount(id, currentUser);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/results")
    @Operation(summary = "Get all detection results for an account")
    public ResponseEntity<List<DetectionResult>> getResults(@PathVariable UUID id) {
        List<DetectionResult> results = detectionRepo.findBySocialAccountIdOrderByAnalyzedAtDesc(id);
        return ResponseEntity.ok(results);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an account and its results")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
