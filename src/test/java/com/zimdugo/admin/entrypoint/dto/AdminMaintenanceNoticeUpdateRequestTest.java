package com.zimdugo.admin.entrypoint.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminMaintenanceNoticeUpdateRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @DisplayName("점검 토글은 관리자 수정 요청에서 필수다")
    void requiresEnabledField() throws Exception {
        AdminMaintenanceNoticeUpdateRequest request = objectMapper.readValue(
            "{\"title\":\"점검 중입니다\",\"message\":\"점검 안내\",\"startedAt\":\"2026-08-30T01:00:00\"}",
            AdminMaintenanceNoticeUpdateRequest.class
        );

        var violations = Validation.buildDefaultValidatorFactory().getValidator().validate(request);

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
            .contains("enabled");
    }
}
