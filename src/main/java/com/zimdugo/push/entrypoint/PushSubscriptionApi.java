package com.zimdugo.push.entrypoint;

import com.zimdugo.core.response.RestResponse;
import com.zimdugo.push.entrypoint.dto.PushSubscriptionUpsertRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Push", description = "웹 푸시 API")
public interface PushSubscriptionApi {

    @Operation(summary = "푸시 구독 등록 또는 갱신")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "등록 또는 갱신 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 구독 정보"),
        @ApiResponse(responseCode = "404", description = "기기 쿠키가 유효하지 않음"),
        @ApiResponse(responseCode = "409", description = "다른 기기에 등록된 푸시 구독")
    })
    @SecurityRequirements
    @PutMapping("/push/subscriptions")
    ResponseEntity<RestResponse<Void>> upsertSubscription(
        @Valid @RequestBody PushSubscriptionUpsertRequest request,
        String deviceToken
    );

    @Operation(summary = "현재 기기 푸시 구독 해제")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "해제 성공 또는 해제할 구독 없음")
    })
    @SecurityRequirements
    @DeleteMapping("/push/subscriptions")
    ResponseEntity<RestResponse<Void>> deleteSubscription(String deviceToken);
}
