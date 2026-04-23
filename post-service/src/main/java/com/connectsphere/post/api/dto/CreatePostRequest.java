package com.connectsphere.post.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.util.StringUtils;

public record CreatePostRequest(
        String userId,
        @NotBlank @Size(max = 500) String caption,
        @Size(max = 500) String mediaUrl,
        @Size(max = 500) String imageUrl
) {

    @AssertTrue(message = "mediaUrl or imageUrl is required")
    public boolean isMediaProvided() {
        return StringUtils.hasText(mediaUrl) || StringUtils.hasText(imageUrl);
    }

    public String resolvedMediaUrl() {
        return StringUtils.hasText(mediaUrl) ? mediaUrl.trim() : imageUrl.trim();
    }

    public String normalizedCaption() {
        return caption.trim();
    }
}
