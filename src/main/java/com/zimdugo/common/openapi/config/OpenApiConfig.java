package com.zimdugo.common.openapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    static {
        SpringDocUtils.getConfig().addAnnotationsToIgnore(
            com.zimdugo.common.security.CurrentUser.class,
            com.zimdugo.common.security.NullableCurrentUser.class
        );
    }

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI zimdugoOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Zimdugo API")
                .description("Zimdugo 백엔드 API 문서"))
            .addServersItem(new Server().url("https://api.zimdugo.com").description("Production"))
            .addServersItem(new Server().url("http://localhost:8080").description("Local"))
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
            .components(new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                    .name(SECURITY_SCHEME_NAME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }

    @Bean
    public OperationCustomizer customizeAcceptLanguageHeader() {
        return (operation, handlerMethod) -> {
            Parameter acceptLanguageHeader = new Parameter()
                .in("header")
                .name("Accept-Language")
                .description("다국어 처리를 위한 언어 설정")
                .required(false)
                .schema(new StringSchema()
                    ._default("ko-KR")
                    .addEnumItem("ko-KR")
                    .addEnumItem("ja-JP")
                    .addEnumItem("en-US")
                    .addEnumItem("zh-CN")
                    .addEnumItem("zh-TW"));

            operation.addParametersItem(acceptLanguageHeader);
            return operation;
        };
    }
}
