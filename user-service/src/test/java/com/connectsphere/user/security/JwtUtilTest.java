package com.connectsphere.user.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.connectsphere.user.config.JwtProperties;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil(new JwtProperties(
            "connectsphere-test-secret-key-2026-change-me",
            "connectsphere-user-service",
            Duration.ofHours(4)
    ));

    @Test
    void generatesValidTokenAndExtractsEmail() {
        String token = jwtUtil.generateToken("baaese@gmail.com");

        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("baaese@gmail.com");
    }
}
