package com.zimdugo.user.entrypoint.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
    @NotBlank(message = "validation.profile_update_required")
    @Pattern(regexp = "^$|^https://.+$", message = "validation.invalid_profile_image_url")
    @Size(max = 500)
    String profileImageUrl
) {
}
