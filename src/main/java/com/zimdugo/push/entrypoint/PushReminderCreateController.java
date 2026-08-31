package com.zimdugo.push.entrypoint;

import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.push.application.PushDeviceTokenHasher;
import com.zimdugo.push.application.PushReminderCreateService;
import com.zimdugo.push.application.PushReminderResult;
import com.zimdugo.push.entrypoint.dto.PushReminderCreateRequest;
import com.zimdugo.push.entrypoint.dto.PushReminderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PushReminderCreateController implements PushReminderCreateApi {

    private final PushReminderCreateService pushReminderCreateService;
    private final PushDeviceTokenHasher pushDeviceTokenHasher;

    @Override
    public ResponseEntity<RestResponse<PushReminderResponse>> createReminder(
        PushReminderCreateRequest request,
        @CookieValue(name = "deviceToken", required = false) String deviceToken
    ) {
        // 원본 토큰 대신 해시만 다음 계층으로 전달해 기기 식별값의 노출 범위를 줄인다.
        PushReminderResult result = pushReminderCreateService.create(
            request.toCommand(pushDeviceTokenHasher.hash(deviceToken == null ? "" : deviceToken))
        );
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, PushReminderResponse.from(result)));
    }
}
