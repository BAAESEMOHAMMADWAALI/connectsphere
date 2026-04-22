package com.connectsphere.post.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotBlank @Size(max = 500) String caption,
        @NotBlank @Size(max = 500) String mediaUrl
) {
}

