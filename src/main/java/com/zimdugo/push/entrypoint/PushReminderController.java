package com.zimdugo.push.entrypoint;

import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.push.application.PushDeviceTokenHasher;
import com.zimdugo.push.application.PushReminderCreateService;
import com.zimdugo.push.application.PushReminderQueryService;
import com.zimdugo.push.application.PushReminderResult;
import com.zimdugo.push.entrypoint.dto.PushReminderCreateRequest;
import com.zimdugo.push.entrypoint.dto.PushReminderResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PushReminderController implements PushReminderCreateApi, PushReminderQueryApi {

    private final PushReminderCreateService pushReminderCreateService;
    private final PushReminderQueryService pushReminderQueryService;
    private final PushDeviceTokenHasher pushDeviceTokenHasher;

    @Override
    public ResponseEntity<RestResponse<PushReminderResponse>> createReminder(
        PushReminderCreateRequest request,
        @CookieValue(name = "deviceToken", required = false) String deviceToken
    ) {
        PushReminderResult result = pushReminderCreateService.create(
            request.toCommand(hashDeviceToken(deviceToken))
        );
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, PushReminderResponse.from(result)));
    }

    @Override
    public ResponseEntity<RestResponse<List<PushReminderResponse>>> getActiveReminders(
        @CookieValue(name = "deviceToken", required = false) String deviceToken
    ) {
        List<PushReminderResponse> responses = pushReminderQueryService.getActive(hashDeviceToken(deviceToken))
            .stream()
            .map(PushReminderResponse::from)
            .toList();
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, responses));
    }

    private String hashDeviceToken(String deviceToken) {
        // 원문 토큰을 저장소 계층으로 넘기지 않아 기기 식별값의 노출 범위를 제한한다.
        return pushDeviceTokenHasher.hash(deviceToken == null ? "" : deviceToken);
    }
}
