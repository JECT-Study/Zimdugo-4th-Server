package com.zimdugo.user.domain;

import java.util.List;

public record UserProfileResponse(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        String status,
        List<String> providers
) {}