package com.zimdugo.auth.domain;

import com.zimdugo.user.domain.AuthProvider;
import com.zimdugo.user.domain.SocialAccount;

public interface SocialAccountUnlinkClient {

    AuthProvider provider();

    void unlink(SocialAccount socialAccount, SocialProviderToken token);
}
