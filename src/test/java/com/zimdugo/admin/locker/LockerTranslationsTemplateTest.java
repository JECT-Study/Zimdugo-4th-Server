package com.zimdugo.admin.locker;

import static org.assertj.core.api.Assertions.assertThat;

import com.zimdugo.admin.locker.dto.AdminLockerDetailResult;
import com.zimdugo.admin.locker.dto.AdminLockerTranslationForm;
import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.publication.PublicationStatus;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

class LockerTranslationsTemplateTest {

    @Test
    void rendersEveryLockerImageOnTheTranslationReviewPage() {
        WebContext context = webContext();
        context.setVariable("locker", lockerWithImages());
        context.setVariable("translationsForm", new AdminLockerTranslationForm());
        context.setVariable("_csrf", new Csrf("test-token", "X-CSRF-TOKEN"));

        String html = templateEngine().process("admin/locker-translations", context);

        assertThat(html)
            .contains("https://cdn.example.com/locker-1.jpg")
            .contains("https://cdn.example.com/locker-2.jpg");
    }

    private AdminLockerDetailResult lockerWithImages() {
        return new AdminLockerDetailResult(
            1L,
            "서울역 보관함",
            "서울 중구",
            37.5,
            127.0,
            null,
            null,
            PublicationStatus.DRAFT,
            LockerType.ETC,
            IndoorOutdoorType.INDOOR,
            null,
            null,
            null,
            null,
            Set.of(),
            null,
            null,
            null,
            List.of("https://cdn.example.com/locker-1.jpg", "https://cdn.example.com/locker-2.jpg"),
            0,
            0
        );
    }

    private SpringTemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    private WebContext webContext() {
        MockServletContext servletContext = new MockServletContext();
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        MockHttpServletResponse response = new MockHttpServletResponse();
        return new WebContext(JakartaServletWebApplication.buildApplication(servletContext)
            .buildExchange(request, response));
    }

    private record Csrf(String token, String headerName) {
    }
}
