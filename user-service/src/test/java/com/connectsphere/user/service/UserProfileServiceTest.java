package com.connectsphere.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectsphere.user.api.dto.LoginRequest;
import com.connectsphere.user.api.dto.RegisterRequest;
import com.connectsphere.user.api.dto.TokenResponse;
import com.connectsphere.user.domain.entity.UserAccount;
import com.connectsphere.user.domain.repository.UserAccountRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserEventPublisher userEventPublisher;

    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    void registerEncryptsPasswordAssignsRoleAndReturnsToken() {
        RegisterRequest request = new RegisterRequest(
                "baaese@gmail.com",
                "Baaese",
                null,
                "123456",
                "https://example.com/avatar.png",
                "Hello ConnectSphere"
        );
        UUID userId = UUID.randomUUID();

        when(userAccountRepository.existsByEmailIgnoreCase("baaese@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("bcrypt-hash");
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> {
            UserAccount savedUser = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedUser, "id", userId);
            ReflectionTestUtils.setField(savedUser, "createdAt", Instant.now());
            return savedUser;
        });
        when(tokenService.issueToken(any(UserAccount.class))).thenReturn("jwt-token");

        TokenResponse response = userProfileService.register(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.userId()).isEqualTo(userId.toString());
        assertThat(response.role()).isEqualTo("USER");

        ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(userCaptor.capture());
        UserAccount savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("baaese@gmail.com");
        assertThat(savedUser.getDisplayName()).isEqualTo("Baaese");
        assertThat(savedUser.getPasswordHash()).isEqualTo("bcrypt-hash");
        assertThat(savedUser.getRole()).isEqualTo("USER");
        assertThat(savedUser.getProfileImageUrl()).isEqualTo("https://example.com/avatar.png");
        verify(userEventPublisher).publishUserRegistered(savedUser);
    }

    @Test
    void loginMatchesPasswordAndReturnsToken() {
        UUID userId = UUID.randomUUID();
        UserAccount userAccount = new UserAccount();
        ReflectionTestUtils.setField(userAccount, "id", userId);
        userAccount.setEmail("baaese@gmail.com");
        userAccount.setDisplayName("Baaese");
        userAccount.setPasswordHash("bcrypt-hash");
        userAccount.setRole("USER");

        when(userAccountRepository.findByEmailIgnoreCase("baaese@gmail.com")).thenReturn(Optional.of(userAccount));
        when(passwordEncoder.matches("123456", "bcrypt-hash")).thenReturn(true);
        when(tokenService.issueToken(userAccount)).thenReturn("jwt-token");

        TokenResponse response = userProfileService.login(new LoginRequest("baaese@gmail.com", "123456"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.userId()).isEqualTo(userId.toString());
        assertThat(response.role()).isEqualTo("USER");
    }
}
