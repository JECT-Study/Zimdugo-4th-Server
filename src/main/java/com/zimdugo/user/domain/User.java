package com.zimdugo.user.domain;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class User {

    private Long id;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private UserStatus status;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User(String email, String nickname, String profileImageUrl, UserStatus status) {
        this(null, email, nickname, profileImageUrl, status, UserRole.USER, null, null);
    }

    public User(String email, String nickname, String profileImageUrl, UserStatus status, UserRole role) {
        this(null, email, nickname, profileImageUrl, status, role, null, null);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    public User(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        UserStatus status,
        UserRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.status = status;
        this.role = role != null ? role : UserRole.USER;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public void changeStatus(UserStatus status) {
        this.status = status;
    }

    public void changeRole(UserRole role) {
        this.role = role != null ? role : UserRole.USER;
    }

    public UserRole getRoleOrDefault() {
        return role != null ? role : UserRole.USER;
    }
}
