package com.zimdugo.user.entrypoint;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
    @Size(max = 50)
    String nickname,

    @Size(max = 500)
    String profileImageUrl
) {
    @AssertTrue(message = "validation.profile_update_required")
    public boolean hasAnyUpdatableField() {
        return hasText(nickname) || hasText(profileImageUrl);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
