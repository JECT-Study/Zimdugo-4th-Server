package com.zimdugo.push.infrastructure.webpush;

import static org.assertj.core.api.Assertions.assertThat;

import com.zimdugo.push.domain.PushLocale;
import com.zimdugo.push.domain.PushNotificationType;
import org.junit.jupiter.api.Test;

class WebPushJavaSenderTextTest {

    @Test
    void returnsLocalizedTimerEndText() {
        assertThat(WebPushJavaSender.title(PushNotificationType.END, PushLocale.KO))
            .isEqualTo("이용 시간이 종료됐어요");
        assertThat(WebPushJavaSender.title(PushNotificationType.END, PushLocale.EN))
            .isEqualTo("Your timer has ended");
        assertThat(WebPushJavaSender.title(PushNotificationType.END, PushLocale.JA))
            .isEqualTo("利用時間が終了しました");
        assertThat(WebPushJavaSender.title(PushNotificationType.END, PushLocale.ZH_HANS))
            .isEqualTo("使用时间已结束");
        assertThat(WebPushJavaSender.title(PushNotificationType.END, PushLocale.ZH_HANT))
            .isEqualTo("使用時間已結束");
    }

    @Test
    void usesLockerNameAsNotificationBody() {
        assertThat(WebPushJavaSender.body("新宿駅コインロッカー")).isEqualTo("新宿駅コインロッカー");
    }
}
