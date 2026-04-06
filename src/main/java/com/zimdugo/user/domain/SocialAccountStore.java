package com.zimdugo.user.domain;

public interface SocialAccountStore {

    SocialAccount store(SocialAccount socialAccount);

    void deleteAllByUserId(Long userId);
}
