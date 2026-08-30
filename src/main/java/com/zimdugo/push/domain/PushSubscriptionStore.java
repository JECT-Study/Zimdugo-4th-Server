package com.zimdugo.push.domain;

public interface PushSubscriptionStore {

    void upsert(Long deviceId, String endpoint, String p256dh, String auth, PushLocale locale);
}
