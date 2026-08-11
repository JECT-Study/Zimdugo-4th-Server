package com.zimdugo.auth.application;

import com.zimdugo.auth.domain.SocialAccountUnlinkClient;
import com.zimdugo.auth.domain.SocialProviderToken;
import com.zimdugo.auth.domain.SocialProviderTokenRepository;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.user.domain.AuthProvider;
import com.zimdugo.user.domain.SocialAccount;
import com.zimdugo.user.domain.SocialAccountReader;
import java.util.ArrayList;
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

    public SocialAccountUnlinkSummary unlinkAll(Long userId) {
        List<SocialAccountUnlinkResult> results = new ArrayList<>();
        for (SocialAccount socialAccount : socialAccountReader.findAllByUserId(userId)) {
            results.add(unlinkSocialAccount(userId, socialAccount));
        }
        return SocialAccountUnlinkSummary.from(results);
    }

    private SocialAccountUnlinkResult unlinkSocialAccount(Long userId, SocialAccount socialAccount) {
        SocialAccountUnlinkClient unlinkClient = unlinkClients.get(socialAccount.getProvider());
        if (unlinkClient == null) {
            logMissingClient(userId, socialAccount);
            return SocialAccountUnlinkResult.skippedUnsupportedProvider(socialAccount.getProvider());
        }

        SocialProviderToken token = socialProviderTokenRepository.find(userId, socialAccount.getProvider())
            .orElse(null);
        if (token == null) {
            logMissingToken(userId, socialAccount);
            return SocialAccountUnlinkResult.skippedMissingToken(socialAccount.getProvider());
        }

        return unlinkWithLogging(userId, socialAccount, unlinkClient, token);
    }

    private SocialAccountUnlinkResult unlinkWithLogging(
        Long userId,
        SocialAccount socialAccount,
        SocialAccountUnlinkClient unlinkClient,
        SocialProviderToken token
    ) {
        try {
            unlinkClient.unlink(socialAccount, token);
            log.info(
                "소셜 연동 해제가 완료되었습니다. userId={}, provider={}",
                userId,
                socialAccount.getProvider()
            );
            return SocialAccountUnlinkResult.unlinked(socialAccount.getProvider());
        } catch (BusinessException exception) {
            if (exception.getErrorCode() != ErrorCode.EXTERNAL_API_ERROR) {
                throw exception;
            }
            log.warn(
                "소셜 연동 해제에 실패했지만 내부 탈퇴 처리는 계속 진행합니다. userId={}, provider={}",
                userId,
                socialAccount.getProvider(),
                exception
            );
            return SocialAccountUnlinkResult.failedExternal(socialAccount.getProvider());
        }
    }

    private void logMissingClient(Long userId, SocialAccount socialAccount) {
        log.warn(
            "연동 해제 클라이언트가 없어 소셜 연동 해제를 건너뜁니다. userId={}, provider={}",
            userId,
            socialAccount.getProvider()
        );
    }

    private void logMissingToken(Long userId, SocialAccount socialAccount) {
        log.warn(
            "저장된 provider token이 없어 소셜 연동 해제를 건너뜁니다. userId={}, provider={}",
            userId,
            socialAccount.getProvider()
        );
    }
}
