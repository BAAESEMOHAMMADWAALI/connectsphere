package com.connectsphere.user.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(min = 3, max = 50) String displayName,
        @Size(max = 280) String bio
) {
}

