package com.zimdugo.user.application;

import java.util.List;

public record UserProfileDto(
    Long id,
    String email,
    String nickname,
    String profileImageUrl,
    String status,
    List<String> providers
) {
}
