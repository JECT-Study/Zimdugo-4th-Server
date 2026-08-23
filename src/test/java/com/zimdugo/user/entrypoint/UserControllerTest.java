package com.zimdugo.user.entrypoint;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zimdugo.common.security.CurrentUser;
import com.zimdugo.user.application.UserProfileDto;
import com.zimdugo.user.application.UserProfileUpdateService;
import com.zimdugo.user.application.UserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private UserProfileUpdateService userProfileUpdateService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController(userQueryService, userProfileUpdateService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(new CurrentUserArgumentResolver())
            .build();
    }

    @Test
    @DisplayName("/me 응답은 email을 포함하고 nickname 필드는 노출하지 않는다")
    void meResponseIncludesEmailAndExcludesNickname() throws Exception {
        given(userQueryService.getProfile(1L)).willReturn(new UserProfileDto(
            1L,
            "zimdugo@gmail.com",
            "https://cdn.zimdugo.com/profile.png",
            "ACTIVE",
            "google"
        ));

        mockMvc.perform(get("/api/v1/me"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data.id").value(1L))
            .andExpect(jsonPath("$.data.email").value("zimdugo@gmail.com"))
            .andExpect(jsonPath("$.data.profileImageUrl").value("https://cdn.zimdugo.com/profile.png"))
            .andExpect(jsonPath("$.data.status").value("ACTIVE"))
            .andExpect(jsonPath("$.data.provider").value("google"))
            .andExpect(jsonPath("$.data.providers").doesNotExist())
            .andExpect(jsonPath("$.data.nickname").doesNotExist());
    }

    private static class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(CurrentUser.class)
                && Long.class.equals(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            org.springframework.web.bind.support.WebDataBinderFactory binderFactory
        ) {
            return 1L;
        }
    }
}
