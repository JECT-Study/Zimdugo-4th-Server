package com.zimdugo.push.entrypoint;

import com.zimdugo.core.response.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name = "Push Device", description = "웹 푸시 기기 식별 API")
public interface PushDeviceApi {

    @Operation(summary = "푸시 기기 초기화")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "기기 초기화 성공"))
    @PostMapping("/push/devices")
    ResponseEntity<RestResponse<Void>> initializeDevice(
        @CookieValue(name = "deviceToken", required = false) String deviceToken,
        HttpServletResponse response
    );
}
