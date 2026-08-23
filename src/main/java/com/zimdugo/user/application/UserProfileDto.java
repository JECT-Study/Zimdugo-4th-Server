package com.zimdugo.user.application;

public record UserProfileDto(
    Long id,
    String email,
    String profileImageUrl,
    String status,
    String provider
) {
}
