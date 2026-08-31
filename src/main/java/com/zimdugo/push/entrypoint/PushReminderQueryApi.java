package com.zimdugo.push.entrypoint;

import com.zimdugo.core.response.RestResponse;
import com.zimdugo.push.entrypoint.dto.PushReminderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Push", description = "웹 푸시 API")
public interface PushReminderQueryApi {

    @Operation(summary = "현재 기기의 활성 리마인더 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @SecurityRequirements
    @GetMapping("/push/reminders")
    ResponseEntity<RestResponse<List<PushReminderResponse>>> getActiveReminders(String deviceToken);
}
