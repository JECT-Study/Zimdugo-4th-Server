package com.zimdugo.locker.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "locker.favorite")
public record FavoriteLockerProperties(
    DefaultOrigin defaultOrigin
) {

    public record DefaultOrigin(
        double latitude,
        double longitude
    ) {
    }
}
