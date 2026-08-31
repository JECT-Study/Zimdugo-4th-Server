package com.zimdugo.push.entrypoint;

import com.zimdugo.core.response.RestResponse;
import com.zimdugo.push.entrypoint.dto.PushReminderCreateRequest;
import com.zimdugo.push.entrypoint.dto.PushReminderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Push", description = "웹 푸시 API")
public interface PushReminderCreateApi {

    @Operation(summary = "푸시 리마인더 생성")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "발화 시각 또는 리마인더 값이 올바르지 않음"),
        @ApiResponse(responseCode = "404", description = "보관함 또는 기기를 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "활성 푸시 구독이 없음")
    })
    @SecurityRequirements
    @PostMapping("/push/reminders")
    ResponseEntity<RestResponse<PushReminderResponse>> createReminder(
        @Valid @RequestBody PushReminderCreateRequest request,
        String deviceToken
    );
}
