package com.zimdugo.push.entrypoint;

import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.push.application.PushDeviceBootstrapResult;
import com.zimdugo.push.application.PushDeviceService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PushDeviceController implements PushDeviceApi {

    private final PushDeviceService pushDeviceService;
    private final PushDeviceCookieFactory pushDeviceCookieFactory;

    @Override
    public ResponseEntity<RestResponse<Void>> initializeDevice(
        @CookieValue(name = PushDeviceCookieFactory.DEVICE_TOKEN_COOKIE_NAME, required = false)
        String deviceToken,
        HttpServletResponse response
    ) {
        PushDeviceBootstrapResult result = pushDeviceService.ensureDevice(deviceToken);
        if (result.issued()) { //발급 필요시
            response.addHeader(HttpHeaders.SET_COOKIE, pushDeviceCookieFactory.create(result.deviceToken()).toString());
        }
        return ResponseEntity.ok(RestResponse.ok(SuccessCode.OK));
    }
}
