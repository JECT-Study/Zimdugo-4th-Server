package com.zimdugo.push.application;

public record PushSubscriptionCommand(
    String deviceTokenHash,
    String endpoint,
    String p256dh,
    String auth,
    String locale
) {
}
