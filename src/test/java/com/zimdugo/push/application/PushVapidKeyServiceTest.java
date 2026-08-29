package com.zimdugo.push.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.zimdugo.push.config.PushVapidProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PushVapidKeyServiceTest {

    @Test
    @DisplayName("클라이언트 구독에 사용할 VAPID 공개키만 반환한다")
    void returnsConfiguredPublicKey() {
        PushVapidProperties properties = new PushVapidProperties();
        properties.setPublicKey("base64url-public-key");
        PushVapidKeyService service = new PushVapidKeyService(properties);

        PushVapidKeyResult result = service.getPublicKey();

        assertThat(result.publicKey()).isEqualTo("base64url-public-key");
    }
}
