package com.zimdugo.push.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushSubscriptionUpsertCoordinator {

    private final PushSubscriptionService pushSubscriptionService;

    public synchronized void upsert(PushSubscriptionCommand command) {
        pushSubscriptionService.upsert(command);
    }
}
