package com.zimdugo.user.domain;

import com.zimdugo.identity.domain.AuthProvider;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class SocialAccount {

    private Long id;
    private User user;
    private AuthProvider provider;
    private String providerUserId;
    private String providerEmail;
    private String providerProfileImageUrl;
    private LocalDateTime linkedAt;

    public SocialAccount(
        User user,
        AuthProvider provider,
        String providerUserId,
        String providerEmail,
        String providerProfileImageUrl
    ) {
        this(null, user, provider, providerUserId, providerEmail, providerProfileImageUrl, null);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    public SocialAccount(
        Long id,
        User user,
        AuthProvider provider,
        String providerUserId,
        String providerEmail,
        String providerProfileImageUrl,
        LocalDateTime linkedAt
    ) {
        this.id = id;
        this.user = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.providerEmail = providerEmail;
        this.providerProfileImageUrl = providerProfileImageUrl;
        this.linkedAt = linkedAt;
    }

    public void updateProviderProfile(String providerEmail, String providerProfileImageUrl) {
        this.providerEmail = providerEmail;
        this.providerProfileImageUrl = providerProfileImageUrl;
    }
}
