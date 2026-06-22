package com.zimdugo.user.infrastructure.config;

import com.zimdugo.user.infrastructure.visitor.VisitorInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("!test")
@RequiredArgsConstructor
public class VisitorWebConfig implements WebMvcConfigurer {

    private final ApplicationEventPublisher eventPublisher;
    private final StringRedisTemplate stringRedisTemplate;

    @Bean
    public VisitorInterceptor visitorInterceptor() {
        return new VisitorInterceptor(eventPublisher, stringRedisTemplate);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(visitorInterceptor())
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/admin/**");
    }
}
