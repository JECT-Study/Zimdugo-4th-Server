package com.zimdugo.locker.entrypoint.config;

import com.zimdugo.locker.entrypoint.converter.IndoorOutdoorTypeRequestConverter;
import com.zimdugo.locker.entrypoint.converter.LockerSizeTypeRequestConverter;
import com.zimdugo.locker.entrypoint.converter.LockerTypeRequestConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LockerRequestWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new LockerSizeTypeRequestConverter());
        registry.addConverter(new IndoorOutdoorTypeRequestConverter());
        registry.addConverter(new LockerTypeRequestConverter());
    }
}
