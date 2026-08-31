package com.zimdugo.push.domain;

public interface WebPushSender {

    WebPushSendResult send(PushSubscription subscription, PushNotificationType type, Long lockerId, String lockerName);
}
