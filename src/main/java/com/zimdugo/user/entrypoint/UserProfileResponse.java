package com.zimdugo.user.entrypoint;

import com.zimdugo.user.application.UserProfileDto;
import java.util.List;

public record UserProfileResponse(
    Long id,
    String email,
    String nickname,
    String profileImageUrl,
    String status,
    List<String> providers
) {
    public static UserProfileResponse from(UserProfileDto dto) {
        return new UserProfileResponse(
            dto.id(),
            dto.email(),
            dto.nickname(),
            dto.profileImageUrl(),
            dto.status(),
            dto.providers()
        );
    }
}
