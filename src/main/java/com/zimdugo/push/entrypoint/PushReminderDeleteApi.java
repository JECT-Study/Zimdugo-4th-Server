package com.zimdugo.push.entrypoint;

import com.zimdugo.core.response.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Push", description = "웹 푸시 API")
public interface PushReminderDeleteApi {

    @Operation(summary = "현재 기기의 리마인더 삭제")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공 또는 삭제할 리마인더 없음")
    })
    @SecurityRequirements
    @DeleteMapping("/push/reminders/{reminderId}")
    ResponseEntity<RestResponse<Void>> deleteReminder(@PathVariable @Positive Long reminderId, String deviceToken);
}
