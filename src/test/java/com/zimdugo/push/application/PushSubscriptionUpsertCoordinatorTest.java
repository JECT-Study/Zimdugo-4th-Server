package com.zimdugo.push.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willAnswer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PushSubscriptionUpsertCoordinatorTest {

    @Mock
    private PushSubscriptionService pushSubscriptionService;

    @Test
    void completesOneUpsertBeforeStartingAnother() throws Exception {
        PushSubscriptionUpsertCoordinator coordinator = new PushSubscriptionUpsertCoordinator(pushSubscriptionService);
        CountDownLatch firstUpsertStarted = new CountDownLatch(1);
        CountDownLatch secondTaskStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstUpsert = new CountDownLatch(1);
        AtomicInteger inProgressCount = new AtomicInteger();
        willAnswer(invocation -> {
            inProgressCount.incrementAndGet();
            firstUpsertStarted.countDown();
            releaseFirstUpsert.await(1, TimeUnit.SECONDS);
            inProgressCount.decrementAndGet();
            return null;
        }).given(pushSubscriptionService).upsert(any());

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<?> first = executor.submit(() -> coordinator.upsert(command()));
            assertThat(firstUpsertStarted.await(1, TimeUnit.SECONDS)).isTrue();

            Future<?> second = executor.submit(() -> {
                secondTaskStarted.countDown();
                coordinator.upsert(command());
            });
            assertThat(secondTaskStarted.await(1, TimeUnit.SECONDS)).isTrue();
            assertThat(inProgressCount).hasValue(1);

            releaseFirstUpsert.countDown();
            first.get(1, TimeUnit.SECONDS);
            second.get(1, TimeUnit.SECONDS);
        }
    }

    private PushSubscriptionCommand command() {
        return new PushSubscriptionCommand(
            "device-token-hash",
            "https://fcm.googleapis.com/fcm/send/example-endpoint",
            "p256dh",
            "auth",
            "ko"
        );
    }
}
