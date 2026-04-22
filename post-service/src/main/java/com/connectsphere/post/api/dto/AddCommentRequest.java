package com.connectsphere.post.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCommentRequest(
        @NotBlank @Size(max = 280) String text
) {
}

