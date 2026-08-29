package com.zimdugo.push.entrypoint;

import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.push.application.PushVapidKeyResult;
import com.zimdugo.push.application.PushVapidKeyService;
import com.zimdugo.push.application.PushDeviceBootstrapResult;
import com.zimdugo.push.application.PushDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CookieValue;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PushVapidKeyController implements PushVapidKeyApi {

    private final PushVapidKeyService pushVapidKeyService;
    private final PushDeviceService pushDeviceService;
    private final PushDeviceCookieFactory pushDeviceCookieFactory;

    @Override
    public ResponseEntity<RestResponse<PushVapidKeyResult>> getVapidPublicKey(
        @CookieValue(name = PushDeviceCookieFactory.DEVICE_TOKEN_COOKIE_NAME, required = false) String deviceToken
    ) {
        // 구독 등록 이전에 VAPID 키 조회가 먼저 와도 이후 리소스의 소유 기기를 식별할 수 있게 한다.
        PushDeviceBootstrapResult device = pushDeviceService.ensureDevice(deviceToken);
        PushVapidKeyResult result = pushVapidKeyService.getPublicKey();
        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        if (device.issued()) {
            // 새 토큰의 원문은 응답 본문이 아닌 HttpOnly 쿠키로만 전달한다.
            response.header(HttpHeaders.SET_COOKIE, pushDeviceCookieFactory.create(device.deviceToken()).toString());
        }
        return response.body(RestResponse.of(SuccessCode.OK, result));
    }
}
