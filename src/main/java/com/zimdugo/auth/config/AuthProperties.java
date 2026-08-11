package com.zimdugo.auth.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private final Callback callback = new Callback();
    private final Cookie cookie = new Cookie();

    @Getter
    @Setter
    public static class Callback {

        private String frontendBaseUrl = "http://localhost:3000";
        private List<String> allowedOrigins = new ArrayList<>();
        private boolean cookieSecure;
    }

    @Getter
    @Setter
    public static class Cookie {

        private final Refresh refresh = new Refresh();

        @Getter
        @Setter
        public static class Refresh {

            private boolean secure;
            private String sameSite = "Strict";
        }
    }
}
