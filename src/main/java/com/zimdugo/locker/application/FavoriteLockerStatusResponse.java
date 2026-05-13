package com.zimdugo.locker.application;

public record FavoriteLockerStatusResponse(
    Long lockerId,
    boolean favorite
) {
}
