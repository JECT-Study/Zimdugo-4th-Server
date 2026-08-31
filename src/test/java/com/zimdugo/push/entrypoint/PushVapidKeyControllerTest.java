package com.zimdugo.push.entrypoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.zimdugo.push.application.PushDeviceBootstrapResult;
import com.zimdugo.push.application.PushDeviceService;
import com.zimdugo.push.application.PushVapidKeyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PushVapidKeyControllerTest {

    @Mock
    private PushVapidKeyService pushVapidKeyService;

    @Mock
    private PushDeviceService pushDeviceService;

    @Mock
    private PushDeviceCookieFactory pushDeviceCookieFactory;

    @InjectMocks
    private PushVapidKeyController controller;

    @Test
    void issuesDeviceTokenWhenVapidKeyIsRequestedWithoutCookie() throws Exception {
        given(pushDeviceService.ensureDevice(null)).willReturn(new PushDeviceBootstrapResult("device-token", true));
        given(pushDeviceCookieFactory.create("device-token"))
            .willReturn(ResponseCookie.from("deviceToken", "device-token").build());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        var response = mockMvc.perform(get("/api/v1/push/vapid-key"))
            .andReturn()
            .getResponse();

        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNotNull();
        assertThat(response.getHeader(HttpHeaders.CACHE_CONTROL)).isEqualTo("no-store");
    }
}
