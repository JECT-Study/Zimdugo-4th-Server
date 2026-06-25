package com.zimdugo.auth.application;

import com.zimdugo.auth.domain.SocialAccountUnlinkClient;
import com.zimdugo.auth.domain.SocialProviderToken;
import com.zimdugo.auth.domain.SocialProviderTokenRepository;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.user.domain.AuthProvider;
import com.zimdugo.user.domain.SocialAccount;
import com.zimdugo.user.domain.SocialAccountReader;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SocialAccountUnlinkService {

    private final SocialAccountReader socialAccountReader;
    private final SocialProviderTokenRepository socialProviderTokenRepository;
    private final Map<AuthProvider, SocialAccountUnlinkClient> unlinkClients;

    public SocialAccountUnlinkService(
        SocialAccountReader socialAccountReader,
        SocialProviderTokenRepository socialProviderTokenRepository,
        List<SocialAccountUnlinkClient> unlinkClients
    ) {
        this.socialAccountReader = socialAccountReader;
        this.socialProviderTokenRepository = socialProviderTokenRepository;
        this.unlinkClients = new EnumMap<>(AuthProvider.class);
        for (SocialAccountUnlinkClient unlinkClient : unlinkClients) {
            this.unlinkClients.put(unlinkClient.provider(), unlinkClient);
        }
    }

    public void unlinkAll(Long userId) {
        for (SocialAccount socialAccount : socialAccountReader.findAllByUserId(userId)) {
            SocialAccountUnlinkClient unlinkClient = unlinkClients.get(socialAccount.getProvider());
            if (unlinkClient == null) {
                throw new BusinessException(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN);
            }

            SocialProviderToken token = socialProviderTokenRepository.find(userId, socialAccount.getProvider())
                .orElseThrow(() -> new BusinessException(ErrorCode.EXTERNAL_API_ERROR));

            unlinkClient.unlink(socialAccount, token);
            log.info(
                "소셜 연동 해제가 완료되었습니다. userId={}, provider={}",
                userId,
                socialAccount.getProvider()
            );
        }
    }
}
