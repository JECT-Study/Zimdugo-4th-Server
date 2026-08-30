package com.zimdugo.push.entrypoint.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

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
}
