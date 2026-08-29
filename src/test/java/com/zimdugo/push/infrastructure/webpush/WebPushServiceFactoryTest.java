package com.zimdugo.push.infrastructure.webpush;

import static org.assertj.core.api.Assertions.assertThat;

import com.zimdugo.push.config.PushVapidProperties;
import nl.martijndwars.webpush.PushService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WebPushServiceFactoryTest {

    @Test
    @DisplayName("VAPID 설정으로 webpush-java PushService를 생성한다")
    void createsPushServiceFromVapidProperties() {
        PushVapidProperties properties = new PushVapidProperties();
        properties.setPublicKey(
            "BGgL7I82SAQM78oyGwaJdrQFhVfZqL9h4Y18BLtgJQ-9pSGXwxqAWQudqmcv41RcWgk1ssUeItv4-8khxbhYveM="
        );
        properties.setPrivateKey("ANlfcVVFB4JiMYcI74_h9h04QZ1Ks96AyEa1yrMgDwn3");
        properties.setSubject("mailto:admin@zimdugo.com");
        WebPushServiceFactory factory = new WebPushServiceFactory(properties);

        PushService pushService = factory.create();

        assertThat(pushService).isNotNull();
    }
}
