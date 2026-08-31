package com.zimdugo.push.domain;

public record PushSubscription(String endpoint, String p256dh, String auth, PushLocale locale) {
}
