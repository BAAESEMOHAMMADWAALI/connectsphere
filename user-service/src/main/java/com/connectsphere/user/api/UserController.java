package com.connectsphere.user.api;

import com.connectsphere.user.api.dto.UpdateProfileRequest;
import com.connectsphere.user.api.dto.UserProfileResponse;
import com.connectsphere.user.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public UserProfileResponse getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) String forwardedUserId,
            JwtAuthenticationToken authentication
    ) {
        return userProfileService.getProfile(resolveUserId(forwardedUserId, authentication));
    }

    @PatchMapping("/me")
    public UserProfileResponse updateCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) String forwardedUserId,
            JwtAuthenticationToken authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return userProfileService.updateProfile(resolveUserId(forwardedUserId, authentication), request);
    }

    private String resolveUserId(String forwardedUserId, JwtAuthenticationToken authentication) {
        if (StringUtils.hasText(forwardedUserId)) {
            return forwardedUserId;
        }
        if (authentication != null) {
            return authentication.getToken().getSubject();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user");
    }
}

