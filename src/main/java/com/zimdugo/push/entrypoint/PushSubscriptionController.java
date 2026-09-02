package com.zimdugo.push.entrypoint;

import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.push.application.PushDeviceTokenHasher;
import com.zimdugo.push.application.PushSubscriptionDeleteService;
import com.zimdugo.push.application.PushSubscriptionUpsertCoordinator;
import com.zimdugo.push.entrypoint.dto.PushSubscriptionUpsertRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PushSubscriptionController implements PushSubscriptionApi {

    private final PushSubscriptionUpsertCoordinator pushSubscriptionUpsertCoordinator;
    private final PushSubscriptionDeleteService pushSubscriptionDeleteService;
    private final PushDeviceTokenHasher pushDeviceTokenHasher;

    @Override
    public ResponseEntity<RestResponse<Void>> upsertSubscription(
        PushSubscriptionUpsertRequest request,
        @CookieValue(name = "deviceToken", required = false) String deviceToken
    ) {
        // 원본 토큰은 영속 계층으로 넘기지 않고 해시 조회로만 기기 소유 여부를 확인한다.
        String deviceTokenHash = pushDeviceTokenHasher.hash(deviceToken == null ? "" : deviceToken);
        pushSubscriptionUpsertCoordinator.upsert(request.toCommand(deviceTokenHash));
        return ResponseEntity.ok(RestResponse.ok(SuccessCode.OK));
    }

    @Override
    public ResponseEntity<RestResponse<Void>> deleteSubscription(
        @CookieValue(name = "deviceToken", required = false) String deviceToken
    ) {
        pushSubscriptionDeleteService.delete(pushDeviceTokenHasher.hash(deviceToken == null ? "" : deviceToken));
        return ResponseEntity.ok(RestResponse.ok(SuccessCode.OK));
    }
}
