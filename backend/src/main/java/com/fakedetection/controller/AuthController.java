package com.fakedetection.controller;

import com.fakedetection.config.JwtUtil;
import com.fakedetection.model.AppUser;
import com.fakedetection.repository.AppUserRepository;
import com.fakedetection.repository.DetectionResultRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication")
class AuthController {

    private final AuthenticationManager authManager;
    private final AppUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(AuthenticationManager authManager, AppUserRepository userRepo,
                          PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.authManager = authManager;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT token")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        AppUser user = userRepo.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(Map.of(
                "accessToken", token,
                "tokenType", "Bearer",
                "expiresIn", 86400,
                "username", user.getUsername(),
                "role", user.getRole().name()
        ));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new analyst account")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }
        AppUser user = AppUser.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(AppUser.Role.ANALYST)
                .build();
        userRepo.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));
    }

    static class LoginRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public void setUsername(String v) { this.username = v; }
        public void setPassword(String v) { this.password = v; }
    }

    static class RegisterRequest {
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
}

@RestController
@RequestMapping("/stats")
@Tag(name = "Statistics")
class StatsController {

    private final DetectionResultRepository detectionRepo;

    @Autowired
    public StatsController(DetectionResultRepository detectionRepo) {
        this.detectionRepo = detectionRepo;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get overall detection statistics")
    public ResponseEntity<Map<String, Object>> getSummary() {
        List<Object[]> grouped = detectionRepo.countGroupedByVerdict();
        Map<String, Long> verdictCounts = new HashMap<>();
        for (Object[] row : grouped) {
            verdictCounts.put(row[0].toString(), (Long) row[1]);
        }
        return ResponseEntity.ok(Map.of(
                "totalAnalyzed", detectionRepo.count(),
                "verdictBreakdown", verdictCounts,
                "averageFakeScore", Optional.ofNullable(detectionRepo.averageFakeScore()).orElse(0.0),
                "totalFake", verdictCounts.getOrDefault("FAKE", 0L),
                "totalReal", verdictCounts.getOrDefault("REAL", 0L),
                "totalSuspicious", verdictCounts.getOrDefault("SUSPICIOUS", 0L)
        ));
    }
}
