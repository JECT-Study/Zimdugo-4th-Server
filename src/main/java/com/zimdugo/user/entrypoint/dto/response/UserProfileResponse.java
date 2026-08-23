package com.zimdugo.user.entrypoint.dto.response;

import com.zimdugo.user.application.UserProfileDto;

public record UserProfileResponse(
    Long id,
    String email,
    String profileImageUrl,
    String status,
    String provider
) {
    public static UserProfileResponse from(UserProfileDto dto) {
        return new UserProfileResponse(
            dto.id(),
            dto.email(),
            dto.profileImageUrl(),
            dto.status(),
            dto.provider()
        );
    }
}
