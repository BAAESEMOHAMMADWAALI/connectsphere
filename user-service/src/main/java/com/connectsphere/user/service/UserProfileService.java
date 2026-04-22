package com.connectsphere.user.service;

import com.connectsphere.user.api.dto.LoginRequest;
import com.connectsphere.user.api.dto.RegisterRequest;
import com.connectsphere.user.api.dto.TokenResponse;
import com.connectsphere.user.api.dto.UpdateProfileRequest;
import com.connectsphere.user.api.dto.UserProfileResponse;
import com.connectsphere.user.domain.entity.UserAccount;
import com.connectsphere.user.domain.repository.UserAccountRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserProfileService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserEventPublisher userEventPublisher;

    public UserProfileService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            UserEventPublisher userEventPublisher
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.userEventPublisher = userEventPublisher;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userAccountRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setEmail(request.email().trim().toLowerCase());
        userAccount.setDisplayName(request.displayName().trim());
        userAccount.setPasswordHash(passwordEncoder.encode(request.password()));
        userAccount.setBio("");

        UserAccount savedUser = userAccountRepository.save(userAccount);
        userEventPublisher.publishUserRegistered(savedUser);

        return new TokenResponse(savedUser.getId().toString(), tokenService.issueToken(savedUser), "Bearer");
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        UserAccount userAccount = userAccountRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), userAccount.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return new TokenResponse(userAccount.getId().toString(), tokenService.issueToken(userAccount), "Bearer");
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String userId) {
        return mapToResponse(findById(userId));
    }

    @Transactional
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        UserAccount userAccount = findById(userId);
        userAccount.setDisplayName(request.displayName().trim());
        userAccount.setBio(request.bio() == null ? "" : request.bio().trim());
        return mapToResponse(userAccount);
    }

    private UserAccount findById(String userId) {
        try {
            return userAccountRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user id", exception);
        }
    }

    private UserProfileResponse mapToResponse(UserAccount userAccount) {
        return new UserProfileResponse(
                userAccount.getId().toString(),
                userAccount.getEmail(),
                userAccount.getDisplayName(),
                userAccount.getBio(),
                userAccount.getCreatedAt()
        );
    }
}

