package com.zimdugo.push.infrastructure.webpush;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.push.domain.PushLocale;
import com.zimdugo.push.config.PushReminderDispatchProperties;
import com.zimdugo.push.domain.PushNotificationType;
import com.zimdugo.push.domain.PushSubscription;
import com.zimdugo.push.domain.WebPushSendResult;
import com.zimdugo.push.domain.WebPushSender;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebPushJavaSender implements WebPushSender {

    private static final int NOT_FOUND_STATUS = 404;
    private static final int GONE_STATUS = 410;
    private static final int SUCCESS_STATUS_MIN = 200;
    private static final int SUCCESS_STATUS_MAX = 299;

    private final WebPushServiceFactory webPushServiceFactory;
    private final ObjectMapper objectMapper;
    private final PushReminderDispatchProperties properties;

    @Override
    public WebPushSendResult send(
        PushSubscription subscription,
        PushNotificationType type,
        Long lockerId,
        String lockerName
    ) {
        try {
            PushService pushService = webPushServiceFactory.create();
            Notification notification = Notification.builder()
                .endpoint(subscription.endpoint())
                .userPublicKey(subscription.p256dh())
                .userAuth(subscription.auth())
                .payload(payload(type, lockerId, subscription.locale(), lockerName))
                .ttl(properties.getDeliveryTtlSeconds())
                .build();
            int statusCode = pushService.send(notification).getStatusLine().getStatusCode();
            if (statusCode == NOT_FOUND_STATUS || statusCode == GONE_STATUS) {
                return WebPushSendResult.SUBSCRIPTION_EXPIRED;
            }
            return statusCode >= SUCCESS_STATUS_MIN && statusCode <= SUCCESS_STATUS_MAX
                ? WebPushSendResult.SENT
                : WebPushSendResult.RETRYABLE_FAILURE;
        } catch (Exception exception) {
            // 작업 완료 여부는 scheduler가 결정하므로 여기서는 재시도 가능한 결과만 반환한다.
            return WebPushSendResult.RETRYABLE_FAILURE;
        }
    }

    private String payload(
        PushNotificationType type,
        Long lockerId,
        PushLocale locale,
        String lockerName
    ) throws JsonProcessingException {
        return objectMapper.writeValueAsString(Map.of(
            "title", title(type, locale),
            "body", body(lockerName),
            "url", "/?locker=" + lockerId
        ));
    }

    static String title(PushNotificationType type, PushLocale locale) {
        return switch (locale) {
            case KO -> switch (type) {
                case START -> "타이머가 시작됐어요";
                case BEFORE_END -> "이용 종료 시간이 얼마 남지 않았어요";
                case END -> "이용 시간이 종료됐어요";
            };
            case EN -> switch (type) {
                case START -> "Your timer has started";
                case BEFORE_END -> "Your timer is ending soon";
                case END -> "Your timer has ended";
            };
            case JA -> switch (type) {
                case START -> "タイマーが開始されました";
                case BEFORE_END -> "利用終了まであとわずかです";
                case END -> "利用時間が終了しました";
            };
            case ZH_HANS -> switch (type) {
                case START -> "计时器已开始";
                case BEFORE_END -> "使用时间即将结束";
                case END -> "使用时间已结束";
            };
            case ZH_HANT -> switch (type) {
                case START -> "計時器已開始";
                case BEFORE_END -> "使用時間即將結束";
                case END -> "使用時間已結束";
            };
        };
    }

    static String body(String lockerName) {
        return lockerName;
    }

}
