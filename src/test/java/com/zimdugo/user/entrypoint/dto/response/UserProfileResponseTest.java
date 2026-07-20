package com.zimdugo.user.entrypoint.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.common.config.JacksonConfig;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.user.application.UserProfileDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileResponseTest {

    private final ObjectMapper objectMapper = new JacksonConfig().objectMapper();

    @Test
    @DisplayName("프로필 응답은 nickname 없이 직렬화된다")
    void excludesNicknameFromProfileResponse() throws Exception {
        UserProfileDto profile = new UserProfileDto(
            1L,
            "zimdugo@gmail.com",
            "https://cdn.zimdugo.com/profile.png",
            "ACTIVE",
            List.of("GOOGLE", "KAKAO")
        );

        JsonNode data = objectMapper.readTree(objectMapper.writeValueAsString(
            RestResponse.of(SuccessCode.OK, UserProfileResponse.from(profile))
        )).path("data");

        assertThat(data.path("email").asText()).isEqualTo("zimdugo@gmail.com");
        assertThat(data.path("profileImageUrl").asText()).isEqualTo("https://cdn.zimdugo.com/profile.png");
        assertThat(data.has("nickname")).isFalse();
    }
}
