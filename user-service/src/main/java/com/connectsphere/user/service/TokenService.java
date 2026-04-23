package com.connectsphere.user.service;

import com.connectsphere.user.domain.entity.UserAccount;
import com.connectsphere.user.security.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final JwtUtil jwtUtil;

    public TokenService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String issueToken(UserAccount userAccount) {
        return jwtUtil.generateToken(userAccount);
    }
}
