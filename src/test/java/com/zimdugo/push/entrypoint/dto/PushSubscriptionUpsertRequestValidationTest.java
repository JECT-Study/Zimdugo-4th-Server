package com.zimdugo.push.entrypoint.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PushSubscriptionUpsertRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsMalformedHttpsEndpoint() {
        PushSubscriptionUpsertRequest request = new PushSubscriptionUpsertRequest(
            "https:// invalid-endpoint",
            new PushSubscriptionUpsertRequest.PushSubscriptionKeys("p256dh", "auth"),
            "ko"
        );

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void rejectsNonBase64UrlSubscriptionKey() {
        PushSubscriptionUpsertRequest request = new PushSubscriptionUpsertRequest(
            "https://fcm.googleapis.com/fcm/send/example",
            new PushSubscriptionUpsertRequest.PushSubscriptionKeys("invalid+/key", "auth"),
            "ko"
        );

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void rejectsBase64UrlKeyThatCannotBeDecoded() {
        PushSubscriptionUpsertRequest request = new PushSubscriptionUpsertRequest(
            "https://fcm.googleapis.com/fcm/send/example",
            new PushSubscriptionUpsertRequest.PushSubscriptionKeys("a=", "auth"),
            "ko"
        );

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "https://fcm.googleapis.com/fcm/send/example",
        "https://updates.push.services.mozilla.com/wpush/v2/example",
        "https://web.push.apple.com/QGdfxw",
        "https://wns2-par02p.notify.windows.com/w/?token=example"
    })
    void acceptsBrowserPushServiceEndpoint(String endpoint) {
        assertThat(validator.validate(request(endpoint))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "https://attacker.example.com/collect",
        "https://fcm.googleapis.com.attacker.example/fcm/send/example",
        "https://fcm.googleapis.com:8443/fcm/send/example",
        "https://user@fcm.googleapis.com/fcm/send/example",
        "https://127.0.0.1/actuator",
        "https://notify.windows.com.attacker.example/w"
    })
    void rejectsEndpointOutsideBrowserPushServices(String endpoint) {
        assertThat(validator.validate(request(endpoint))).isNotEmpty();
    }

    private PushSubscriptionUpsertRequest request(String endpoint) {
        return new PushSubscriptionUpsertRequest(
            endpoint,
            new PushSubscriptionUpsertRequest.PushSubscriptionKeys("p256dh", "auth"),
            "ko"
        );
    }
}
