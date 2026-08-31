package com.zimdugo.push.domain;

public interface PushSubscriptionReader {

    boolean existsByDeviceId(Long deviceId);
}
