package com.zimdugo.push.infrastructure.webpush;

import com.zimdugo.push.config.PushVapidProperties;
import java.security.GeneralSecurityException;
import java.security.Security;
import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebPushServiceFactory {

    private static final String BOUNCY_CASTLE_PROVIDER_NAME = "BC";

    private final PushVapidProperties pushVapidProperties;

    public PushService create() {
        addBouncyCastleProviderIfAbsent();
        try {
            return new PushService(
                pushVapidProperties.getPublicKey(),
                pushVapidProperties.getPrivateKey(),
                pushVapidProperties.getSubject()
            );
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("VAPID 키 설정이 올바르지 않습니다.", exception);
        }
    }

    private void addBouncyCastleProviderIfAbsent() {
        if (Security.getProvider(BOUNCY_CASTLE_PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
}
