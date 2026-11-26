package com.zimdugo.push.infrastructure.webpush;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.push.config.PushReminderDispatchProperties;
import com.zimdugo.push.domain.PushLocale;
import com.zimdugo.push.domain.PushNotificationType;
import com.zimdugo.push.domain.PushSubscription;
import com.zimdugo.push.domain.WebPushSendResult;
import java.security.Security;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.message.BasicHttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WebPushJavaSenderTest {

    private static final String USER_PUBLIC_KEY =
        "BGgL7I82SAQM78oyGwaJdrQFhVfZqL9h4Y18BLtgJQ-9pSGXwxqAWQudqmcv41RcWgk1ssUeItv4-8khxbhYveM";
    private static final String USER_AUTH = "AAAAAAAAAAAAAAAAAAAAAA";
    private static final long TIMEOUT_MILLIS = 100L;

    private final WebPushServiceFactory webPushServiceFactory = mock(WebPushServiceFactory.class);
    private final PushService pushService = mock(PushService.class);
    private WebPushJavaSender sender;

    @BeforeAll
    static void registerProvider() {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @BeforeEach
    void setUp() {
        PushReminderDispatchProperties properties = new PushReminderDispatchProperties();
        properties.setDeliveryTtlSeconds(60);
        properties.setDeliveryTimeoutMillis(TIMEOUT_MILLIS);
        sender = new WebPushJavaSender(webPushServiceFactory, new ObjectMapper(), properties);
        given(webPushServiceFactory.create()).willReturn(pushService);
    }

    @Test
    @DisplayName("푸시 서비스가 응답하지 않으면 제한 시간 뒤 요청을 취소하고 재시도 대상으로 반환한다")
    void cancelsHangingDeliveryAfterTimeout() throws Exception {
        CompletableFuture<HttpResponse> hangingResponse = new CompletableFuture<>();
        given(pushService.sendAsync(any(Notification.class))).willReturn(hangingResponse);

        long startedAt = System.nanoTime();
        WebPushSendResult result = send();
        Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);

        assertThat(result).isEqualTo(WebPushSendResult.RETRYABLE_FAILURE);
        assertThat(hangingResponse.isCancelled()).isTrue();
        assertThat(elapsed).isLessThan(Duration.ofSeconds(2));
    }

    @Test
    @DisplayName("응답 대기 중 인터럽트되면 요청을 취소하고 인터럽트 상태를 유지한다")
    void cancelsDeliveryWhenInterrupted() throws Exception {
        CompletableFuture<HttpResponse> hangingResponse = new CompletableFuture<>();
        given(pushService.sendAsync(any(Notification.class))).willReturn(hangingResponse);

        Thread.currentThread().interrupt();
        WebPushSendResult result = send();
        boolean interrupted = Thread.interrupted();

        assertThat(result).isEqualTo(WebPushSendResult.RETRYABLE_FAILURE);
        assertThat(hangingResponse.isCancelled()).isTrue();
        assertThat(interrupted).isTrue();
    }

    @Test
    @DisplayName("푸시 서비스가 2xx로 응답하면 발송 성공으로 반환한다")
    void returnsSentForSuccessfulResponse() throws Exception {
        given(pushService.sendAsync(any(Notification.class)))
            .willReturn(CompletableFuture.completedFuture(response(201)));

        assertThat(send()).isEqualTo(WebPushSendResult.SENT);
    }

    @Test
    @DisplayName("구독이 만료됐다는 410 응답은 구독 만료로 반환한다")
    void returnsExpiredForGoneResponse() throws Exception {
        given(pushService.sendAsync(any(Notification.class)))
            .willReturn(CompletableFuture.completedFuture(response(410)));

        assertThat(send()).isEqualTo(WebPushSendResult.SUBSCRIPTION_EXPIRED);
    }

    @Test
    @DisplayName("푸시 서비스의 5xx 응답은 재시도 대상으로 반환한다")
    void returnsRetryableForServerError() throws Exception {
        given(pushService.sendAsync(any(Notification.class)))
            .willReturn(CompletableFuture.completedFuture(response(503)));

        assertThat(send()).isEqualTo(WebPushSendResult.RETRYABLE_FAILURE);
    }

    private WebPushSendResult send() {
        PushSubscription subscription = new PushSubscription(
            "https://fcm.googleapis.com/fcm/send/example", USER_PUBLIC_KEY, USER_AUTH, PushLocale.KO
        );
        return sender.send(subscription, PushNotificationType.END, 1L, "서울역 물품보관함");
    }

    private HttpResponse response(int statusCode) {
        return new BasicHttpResponse(HttpVersion.HTTP_1_1, statusCode, null);
    }
}
