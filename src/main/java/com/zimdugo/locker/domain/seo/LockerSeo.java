package com.zimdugo.locker.domain.seo;

import java.util.Map;

public record LockerSeo(
    Long id,
    Map<String, String> names
) {}
