package com.zimdugo.push.entrypoint.dto;

import com.zimdugo.push.application.PushSubscriptionCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record PushSubscriptionUpsertRequest(
    @NotBlank @URL(protocol = "https") @Size(max = 2048) String endpoint,
    @NotNull @Valid PushSubscriptionKeys keys,
    @NotBlank @Pattern(regexp = "ko|en|ja|zh-Hans|zh-Hant") String locale
) {
    public PushSubscriptionCommand toCommand(String deviceTokenHash) {
        // 브라우저가 만든 키는 Web Push 암호화에 사용되므로 서버에서 정규화하지 않고 그대로 보존한다.
        return new PushSubscriptionCommand(deviceTokenHash, endpoint, keys.p256dh(), keys.auth(), locale);
    }

    public record PushSubscriptionKeys(
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]+={0,2}$") @ValidBase64Url @Size(max = 512) String p256dh,
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]+={0,2}$") @ValidBase64Url @Size(max = 128) String auth
    ) {
    }
}
