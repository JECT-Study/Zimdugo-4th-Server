package com.zimdugo.push.entrypoint;

import com.zimdugo.core.response.RestResponse;
import com.zimdugo.push.application.PushVapidKeyResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Push VAPID", description = "웹 푸시 VAPID 공개키 API")
public interface PushVapidKeyApi {

    @Operation(summary = "VAPID 공개키 조회")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "공개키 조회 성공"))
    @GetMapping("/push/vapid-key")
    ResponseEntity<RestResponse<PushVapidKeyResult>> getVapidPublicKey(String deviceToken);
}
